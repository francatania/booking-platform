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
              │                  │              │
              │                  │   HTTP ◄─────┘
              │                  │  (validate service)
              │                  │
    ┌─────────▼──────────────────▼──────────────────────────┐
    │                    PostgreSQL                          │
    │         auth_db  │  company_db  │  booking_db          │
    └───────────────────────────────────────────────────────┘
```

**Key design decisions:**
- **Database per service** -- each microservice owns its data, no shared tables
- **JWT-based auth** -- stateless, tokens validated locally by each service
- **Service-to-service REST** -- booking-service calls company-service internally to validate services
- **Gateway pattern** -- single entry point, clients never talk to services directly

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

**Roles:** `USER`, `ADMIN`, `SUPER_ADMIN`

JWT payload includes: `userId`, `email`, `role`, `companyId`

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
| GET | `/companies/ping` | Public | Health check |

**Ownership check:** ADMINs can only manage services from their own company.

---

### booking-service

Handles reservations with anti-collision logic.

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/bookings` | Authenticated | Create booking |
| GET | `/bookings/my` | Authenticated | List user's bookings |
| GET | `/bookings/{id}` | Authenticated | Get booking details |
| PATCH | `/bookings/{id}/cancel` | Owner | Cancel booking |
| GET | `/bookings/ping` | Public | Health check |

**Business rules:**
- Users book for themselves; admins can book on behalf of any user
- Validates that the service exists and is active (HTTP call to company-service)
- **Anti-collision:** prevents overlapping bookings for the same user
- **Configurable gap:** per-company gap between bookings (e.g., 15 min for barber, 30 min for spa)

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
| User | Email | Password | Role |
|------|-------|----------|------|
| superadmin | superadmin@test.com | password123 | SUPER_ADMIN |
| admin | admin@test.com | password123 | ADMIN (company 1) |
| user | user@test.com | password123 | USER |

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
