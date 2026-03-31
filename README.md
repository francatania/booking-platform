# Booking Platform

Microservices-based booking platform for scheduling appointments at service-oriented businesses (barbershops, spas, studios, etc.).

Built with **Spring Boot**, **FastAPI**, **Nginx**, **PostgreSQL** and **Docker Compose**.

---

## Architecture

```
                         ┌──────────────────┐
                         │     Client       │
                         │  (Postman/Web)   │
                         └────────┬─────────┘
                                  │ :80
                         ┌────────▼─────────┐
                         │   Nginx Gateway  │
                         │   Rate Limiting  │
                         │   Reverse Proxy  │
                         └──┬─────┬──────┬──┘
                            │     │      │
              ┌─────────────┘     │      └─────────────┐
              │                   │                    │
    ┌─────────▼──────┐  ┌────────▼───────┐  ┌─────────▼──────┐
    │  auth-service   │  │ company-service │  │ booking-service │
    │  Spring Boot    │  │  Spring Boot    │  │    FastAPI       │
    │  :8081          │  │  :8082          │  │    :8000         │
    └─────────┬──────┘  └────────┬───────┘  └──┬──────────────┘
              │                  │   ▲          │
              │ ◄────────────────┼───┼──────────┘
              │  (GET /internal/ │   │  HTTP calls:
              │   users?ids=)    │   │  - validate service
              │                  │   │  - GET /internal/services?ids=
              │                  │   │    (API Composition)
    ┌─────────▼──────────────────▼──────────────────────────┐
    │                    PostgreSQL                          │
    │         auth_db  │  company_db  │  booking_db          │
    └───────────────────────────────────────────────────────┘
```

**Key design decisions:**
- **Database per service** — each microservice owns its data, no shared tables
- **JWT-based auth** — stateless, tokens validated locally by each service
- **API Composition** — when the operator panel fetches bookings, booking-service makes two parallel batch calls (`/internal/users?ids=` and `/internal/services?ids=`) to enrich the response with names, avoiding N+1 calls
- **Gateway pattern** — single entry point, `/internal/*` is blocked externally (403)

---

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Auth Service | Java 17, Spring Boot 3.2, Spring Security, JJWT |
| Company Service | Java 17, Spring Boot 3.2, Spring Security, JJWT |
| Booking Service | Python 3.12, FastAPI, SQLAlchemy, httpx |
| Gateway | Nginx 1.25 |
| Database | PostgreSQL 16 |
| Containerization | Docker, Docker Compose |
| Testing | JUnit + Mockito (Java), pytest + unittest.mock (Python) |

---

## Services

### auth-service

Handles user registration, login and JWT issuance.

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/auth/register` | Public | Register a new user |
| POST | `/auth/register-admin` | SUPER_ADMIN | Register an admin with company assignment |
| POST | `/auth/login` | Public | Login, returns JWT |
| GET | `/auth/ping` | Public | Health check |

**Roles:** `USER`, `OPERATOR`, `MANAGER`, `ADMIN`, `SUPER_ADMIN`

JWT payload includes: `userId`, `username`, `email`, `role`, `companyId`

**Internal endpoint (not accessible via gateway):**
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/internal/users?ids=1,2,3` | Batch fetch user names (used by booking-service) |

---

### company-service

Manages companies and their service catalog.

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/companies` | Public | List all companies |
| GET | `/companies/{id}` | Public | Company details |
| GET | `/companies/{id}/services` | Public | List company services |
| POST | `/companies` | SUPER_ADMIN | Create company |
| POST | `/companies/{id}/services` | ADMIN (owner) | Create service |
| PATCH | `/services/{id}` | ADMIN (owner) | Update service |
| PATCH | `/services/{id}/activate` | ADMIN (owner) | Activate service |
| PATCH | `/services/{id}/deactivate` | ADMIN (owner) | Deactivate service |
| GET | `/internal/services/{id}` | Internal only | Validate service (blocked by gateway) |
| GET | `/internal/services?ids=1,2,3` | Internal only | Batch fetch service names (blocked by gateway) |
| GET | `/companies/ping` | Public | Health check |

**Ownership check:** ADMINs can only manage services from their own company.

---

### booking-service

Handles reservations with anti-collision logic.

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/bookings` | USER | Create booking |
| GET | `/bookings/my` | USER | List own bookings |
| GET | `/bookings/{id}` | Authenticated | Get booking details |
| PATCH | `/bookings/{id}/cancel` | USER (owner) / ADMIN | Cancel booking |
| PATCH | `/bookings/{id}/reschedule` | USER (owner) | Reschedule booking |
| GET | `/bookings/company` | OPERATOR / ADMIN | List company bookings (with user & service names) |
| PATCH | `/bookings/{id}/confirm` | OPERATOR | Confirm a pending booking |
| PATCH | `/bookings/{id}/complete` | OPERATOR | Mark a confirmed booking as completed |
| GET | `/bookings/stats` | ADMIN / MANAGER | Aggregated stats: counts, revenue, period breakdown |
| GET | `/bookings/ping` | Public | Health check |

**Booking lifecycle:**
```
PENDING → CONFIRMED → COMPLETED
   └──────────────────────────→ CANCELLED (from any state)
```

**Role rules:**
- `USER` — creates and manages their own bookings
- `OPERATOR` — sees all company bookings, can confirm and complete them
- `MANAGER` / `ADMIN` — read-only access to stats dashboard
- `SUPER_ADMIN` — platform-level role (creates companies, registers admins); not intended for booking operations

**Anti-collision logic (two independent checks):**
1. **Global overlap check** — the user cannot have two bookings that overlap in time, regardless of company. If any existing active booking conflicts with the requested time window → `409`
2. **Company gap check** — each company has a configurable `gap_minutes` (stored in `booking_config`). If the requested booking starts within `gap_minutes` of another booking at the same company → `409`

Example: Company A has `gap_minutes = 30`. If a user has a booking at Company A ending at 10:00, a new booking at Company A starting at 10:20 would be rejected (only 20 min gap, less than the required 30).

**Other rules:**
- Service must exist and be active (validated via HTTP call to company-service)
- `start_time` must be before `end_time`
- Cancelled bookings cannot be cancelled or rescheduled again

---

## Gateway (Nginx)

Single entry point on port 80. All external traffic flows through here.

**Routing:**
| Path | Upstream |
|------|----------|
| `/auth/*` | auth-service:8081 |
| `/companies/*` | company-service:8082 |
| `/services/*` | company-service:8082 |
| `/bookings/*` | booking-service:8000 |
| `/internal/*` | Blocked (403) |
| `/health` | Gateway status |

**Rate limiting:**
- Auth endpoints: **5 req/min** (brute force protection)
- API endpoints: **30 req/s**
- Returns `429 Too Many Requests` when exceeded

**Other features:** gzip compression, proxy timeouts, hidden server tokens

---

## Running the Project

### Prerequisites
- Docker and Docker Compose

### Start everything
```bash
docker compose up --build
```

This will:
1. Start PostgreSQL and create 3 databases (`auth_db`, `company_db`, `booking_db`)
2. Run the seed script with test users, companies, services and bookings
3. Build and start all three services (running unit tests during build)
4. Start the Nginx gateway

### Default test credentials
| Username | Email | Password | Role |
|----------|-------|----------|------|
| superadmin | superadmin@test.com | password123 | SUPER_ADMIN |
| admin | admin@test.com | password123 | ADMIN (company 1) |
| operator1 | operator@test.com | password123 | OPERATOR (company 1) |
| user | user@test.com | password123 | USER |

### Happy path (backend walkthrough)

A full end-to-end flow using Postman or curl. All requests go to `http://localhost`.

**1. Login as a USER and create a booking**
```
POST /auth/login
{ "username": "user", "password": "password123" }
→ copy the token
```
```
GET /companies
→ pick a companyId (e.g. 1)

GET /companies/1/services
→ pick a serviceId and its price (e.g. serviceId: 1, price: 5000)
```
```
POST /bookings
Authorization: Bearer <token>
{
  "company_id": 1,
  "service_id": 1,
  "price": 5000,
  "start_time": "2026-04-10T10:00:00",
  "end_time": "2026-04-10T10:30:00"
}
→ 201 Created, status: PENDING
```

**2. View your bookings**
```
GET /bookings/my
Authorization: Bearer <user token>
→ list of bookings with status PENDING
```

**3. Login as OPERATOR and manage the booking**
```
POST /auth/login
{ "username": "operator1", "password": "password123" }
→ copy the operator token
```
```
GET /bookings/company?status=PENDING
Authorization: Bearer <operator token>
→ returns enriched list with user full name and service name
```
```
PATCH /bookings/{id}/confirm
Authorization: Bearer <operator token>
→ status changes to CONFIRMED
```
```
PATCH /bookings/{id}/complete
Authorization: Bearer <operator token>
→ status changes to COMPLETED
```

**4. View stats as ADMIN**
```
POST /auth/login
{ "username": "admin", "password": "password123" }
→ copy the admin token
```
```
GET /bookings/stats?from_date=2026-01-01T00:00:00&to_date=2026-12-31T23:59:59
Authorization: Bearer <admin token>
→ returns total bookings, revenue, breakdown by status and by day
```

### Useful commands
```bash
docker compose up -d                    # Start in background
docker compose up --build gateway       # Rebuild a single service
docker compose down                     # Stop all containers
docker compose down -v                  # Stop and reset databases
docker compose logs -f booking-service  # Follow logs of a service
```

### Testing with Postman
Import `booking-platform.postman_collection.json`. The collection uses `http://localhost` as base URL and auto-captures the JWT token on login.

---

## Testing

Unit tests run automatically during Docker build. If any test fails, the image won't be built.

**Run tests locally:**

```bash
# Java services
cd auth-service && mvn test
cd company-service && mvn test

# Python service
cd booking-service
source venv/bin/activate
pytest tests/ --cov=app --cov-report=term-missing
```

---

## Project Structure

```
booking-platform/
├── auth-service/          # Spring Boot - Authentication & JWT
├── company-service/       # Spring Boot - Companies & Services
├── booking-service/       # FastAPI - Reservations
├── gateway/               # Nginx config
├── docker/                # DB init script with seed data
├── docker-compose.yml     # Full stack orchestration
└── booking-platform.postman_collection.json
```
