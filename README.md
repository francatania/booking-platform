# Booking Platform

Microservices-based booking platform for scheduling appointments at service-oriented businesses (barbershops, spas, studios, etc.).

Built with **Spring Boot**, **FastAPI**, **Node.js**, **Angular**, **RabbitMQ**, **Nginx**, **PostgreSQL** and **Docker Compose**.

---

## Architecture

```
                         ┌──────────────────────┐
                         │   Angular Client      │
                         │   (SPA - port 4200)   │
                         └──────────┬────────────┘
                                    │ HTTP :80
                         ┌──────────▼────────────┐
                         │     Nginx Gateway      │
                         │   Rate Limiting        │
                         │   Reverse Proxy        │
                         └──┬──────┬────────┬────┘
                            │      │        │
          ┌─────────────────┘      │        └────────────────┐
          │                        │                         │
┌─────────▼──────┐      ┌──────────▼──────┐      ┌──────────▼──────┐
│  auth-service  │      │ company-service  │      │ booking-service  │
│  Spring Boot   │      │  Spring Boot     │      │    FastAPI       │
│  :8081         │      │  :8082           │      │    :8000         │
└─────────┬──────┘      └──────────┬───────┘      └──────┬──────────┘
          │                        │   ▲                  │
          │ ◄──────────────────────┼───┼──────────────────┘
          │  (GET /internal/users) │   │  HTTP calls:
          │                        │   │  - validate service
          │                        │   │  - GET /internal/services
          │                        │   │
          │                        │             ┌─────────────────────┐
          │                        │             │   RabbitMQ          │
          │                        │             │   booking_events     │
          │                        │             │   (topic exchange)   │
          │                        │             └──────────┬──────────┘
          │                        │                        │
          │                        │             ┌──────────▼──────────┐
          │                        │             │ notification-service │
          │◄───────────────────────┼─────────────│   Node.js / Express  │
          │  (GET /internal/users) │             │   :3000              │
          │                        │             └─────────────────────┘
          │                        │
┌─────────▼────────────────────────▼──────────────────────────────────┐
│                           PostgreSQL                                  │
│     auth_db  │  company_db  │  booking_db  │  notification_db        │
└───────────────────────────────────────────────────────────────────────┘
```

**Key design decisions:**
- **Database per service** — each microservice owns its data, no shared tables
- **JWT-based auth** — stateless, tokens validated locally by each service
- **API Composition** — when the operator panel fetches bookings, booking-service makes two parallel batch calls (`/internal/users?ids=` and `/internal/services?ids=`) to enrich the response with names, avoiding N+1 calls
- **Gateway pattern** — single entry point, `/internal/*` is blocked externally (403)
- **Event-driven notifications** — booking-service publishes domain events to RabbitMQ after DB commit; notification-service consumes them asynchronously and sends in-app notifications + emails
- **Fire-and-forget events** — if the broker is down, the booking succeeds and the notification is silently skipped (acceptable tradeoff)

---

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Auth Service | Java 17, Spring Boot 3.2, Spring Security, JJWT |
| Company Service | Java 17, Spring Boot 3.2, Spring Security, JJWT |
| Booking Service | Python 3.12, FastAPI, SQLAlchemy, httpx, pika |
| Notification Service | Node.js, Express, amqplib, nodemailer, pg |
| Frontend | Angular 18, Angular Material, Tailwind CSS, ngx-translate |
| Gateway | Nginx 1.25 |
| Message Broker | RabbitMQ 3 (topic exchange) |
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

**Internal endpoints (not accessible via gateway):**
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/internal/users?ids=1,2,3` | Batch fetch user names (used by booking-service) |
| GET | `/internal/users/{id}` | Fetch single user (used by notification-service) |
| GET | `/internal/users/by-company?companyId=1&role=OPERATOR` | Fetch users by company and role (used by notification-service) |

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
| GET | `/companies/ping` | Public | Health check |

**Internal endpoints (not accessible via gateway):**
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/internal/services/{id}` | Validate service exists and is active |
| GET | `/internal/services?ids=1,2,3` | Batch fetch service names |

**Ownership check:** ADMINs can only manage services from their own company.

---

### booking-service

Handles reservations with anti-collision logic and publishes domain events after state transitions.

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/bookings` | USER | Create booking → publishes `booking.created` |
| GET | `/bookings/my` | USER | List own bookings |
| GET | `/bookings/{id}` | Authenticated | Get booking details |
| PATCH | `/bookings/{id}/cancel` | USER (owner) / OPERATOR / ADMIN | Cancel booking → publishes `booking.cancelled` |
| PATCH | `/bookings/{id}/reschedule` | USER (owner) | Reschedule booking |
| GET | `/bookings/company` | OPERATOR / ADMIN | List company bookings (enriched with user & service names) |
| PATCH | `/bookings/{id}/confirm` | OPERATOR / ADMIN | Confirm pending booking → 204, publishes `booking.confirmed` |
| PATCH | `/bookings/{id}/complete` | OPERATOR / ADMIN | Complete confirmed booking → 204 |
| GET | `/bookings/stats` | ADMIN / MANAGER | Aggregated stats: counts, revenue, period breakdown |
| GET | `/bookings/ping` | Public | Health check |

**Booking lifecycle:**
```
PENDING → CONFIRMED → COMPLETED
   └──────────────────────────→ CANCELLED
```
State transitions are enforced by a dedicated `BookingStateMachine` module. Invalid transitions return `409`.

**Role rules:**
- `USER` — creates and manages their own bookings
- `OPERATOR` / `ADMIN` — sees all company bookings, can confirm, complete and cancel them
- `MANAGER` — read-only access to stats dashboard
- `SUPER_ADMIN` — platform-level role (creates companies, registers admins)

**Anti-collision logic (two independent checks):**
1. **Global overlap check** — the user cannot have two bookings that overlap in time, regardless of company. If any existing active booking conflicts with the requested time window → `409`
2. **Company gap check** — each company has a configurable `gap_minutes` (stored in `booking_config`). If the requested booking starts within `gap_minutes` of another booking at the same company → `409`

Example: Company A has `gap_minutes = 30`. If a user has a booking at Company A ending at 10:00, a new booking at Company A starting at 10:20 would be rejected (only 20 min gap, less than the required 30).

**Other rules:**
- Service must exist and be active (validated via HTTP call to company-service)
- `start_time` must be before `end_time`
- Cancelled bookings cannot be cancelled or rescheduled again

---

### notification-service

Consumes booking domain events from RabbitMQ and delivers in-app notifications and emails.

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/api/notifications` | Authenticated | List own notifications |
| GET | `/api/notifications/unread-count` | Authenticated | Unread notification count |
| PATCH | `/api/notifications/:id/read` | Authenticated | Mark one as read |
| PATCH | `/api/notifications/read-all` | Authenticated | Mark all as read |
| GET | `/notifications/ping` | Public | Health check |

**Event handling:**

| Routing key | Who gets notified | Email sent |
|-------------|------------------|------------|
| `booking.created` | All OPERATORs + ADMINs of the company | Yes |
| `booking.confirmed` | The user who made the booking | Yes |
| `booking.cancelled` (by user) | All OPERATORs + ADMINs of the company | Yes |
| `booking.cancelled` (by operator/admin) | The booking owner | Yes |

**In-app messages** are stored as structured data (`serviceName;date;startTime`) and translated client-side using the active language. Email templates are rendered server-side in `es`/`en` based on the `Accept-Language` header sent at booking creation time.

---

## Design Patterns

### booking-service

**State Machine** (`app/state_machine.py`)
Centralizes all booking status transition rules in an explicit `TRANSITIONS` map. `BookingService` delegates all transition validation to a single `transition(booking, target)` function. Adding a new state only requires updating the map.

```python
TRANSITIONS = {
    BookingStatus.PENDING:   {BookingStatus.CONFIRMED, BookingStatus.CANCELLED},
    BookingStatus.CONFIRMED: {BookingStatus.COMPLETED, BookingStatus.CANCELLED},
    BookingStatus.COMPLETED: set(),
    BookingStatus.CANCELLED: set(),
}
```

**Repository** (`app/repositories/booking_repository.py`)
Isolates all SQLAlchemy queries from business logic. `BookingRepository` is instantiated once per request via FastAPI's dependency injection (`get_repo = Depends(get_db)`). `BookingService` receives a `BookingRepository` and has no knowledge of the ORM.

```
Router → Service → StateMachine
  ↓
get_repo()  →  BookingRepository  →  PostgreSQL
```

### notification-service

**Handler Map** (`src/consumers/bookingConsumer.js`)
Each routing key maps to an async handler that resolves recipients and notification metadata. The consumer loop is generic and routing-key-agnostic — adding a new event type only requires adding a new entry to the `handlers` object.

```js
const handlers = {
  'booking.created':   async (event) => ({ users: await fetchCompanyStaff(...), ... }),
  'booking.confirmed': async (event) => ({ users: [await fetchUser(...)], ... }),
  'booking.cancelled': async (event) => ({ users: cancelledByStaff ? [user] : staff, ... }),
};
```

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
| `/api/notifications/*` | notification-service:3000 |
| `/notifications/ping` | notification-service:3000 |
| `/internal/*` | Blocked (403) |
| `/health` | Gateway status |

**Rate limiting:**
- Auth endpoints: **5 req/min** (brute force protection)
- API endpoints: **30 req/s**
- Returns `429 Too Many Requests` when exceeded

**Other features:** gzip compression, CORS headers, proxy timeouts, hidden server tokens

---

## Running the Project

### Prerequisites
- Docker and Docker Compose
- (Optional) Gmail app password for email notifications

### Environment variables

Create a `.env` file at the project root for SMTP support:

```env
SMTP_USER=your-gmail@gmail.com
SMTP_PASS=your-app-password
```

Without these variables the service still works — notifications are created in-app but emails are skipped.

### Start everything
```bash
docker compose up --build
```

This will:
1. Start PostgreSQL and create 4 databases (`auth_db`, `company_db`, `booking_db`, `notification_db`)
2. Run the seed script with test users, companies, services and bookings
3. Start RabbitMQ
4. Build and start all services (unit tests run during build for Java/Python services)
5. Start the Nginx gateway

### Default test credentials
| Username | Email | Password | Role |
|----------|-------|----------|------|
| superadmin | superadmin@test.com | password123 | SUPER_ADMIN |
| admin | admin@test.com | password123 | ADMIN (company 1) |
| operator1 | operator@test.com | password123 | OPERATOR (company 1) |
| user | user@test.com | password123 | USER |

### Happy path (backend walkthrough)

**1. Login as a USER and create a booking**
```
POST /auth/login
{ "username": "user", "password": "password123" }
→ copy the token
```
```
GET /companies             → pick a companyId
GET /companies/1/services  → pick a serviceId and price
```
```
POST /bookings
Authorization: Bearer <token>
{
  "company_id": 1,
  "service_id": 1,
  "price": 5000,
  "service_name": "Haircut",
  "start_time": "2026-04-10T10:00:00",
  "end_time": "2026-04-10T10:30:00"
}
→ 201 Created, status: PENDING
→ OPERATORs and ADMINs of the company receive an in-app notification + email
```

**2. Login as OPERATOR and manage the booking**
```
POST /auth/login  { "username": "operator1", "password": "password123" }

GET /bookings/company?status=PENDING
→ enriched list with user full name and service name

PATCH /bookings/{id}/confirm   → 204, status: CONFIRMED
→ the booking owner receives an in-app notification + email

PATCH /bookings/{id}/complete  → 204, status: COMPLETED
```

**3. View stats as ADMIN**
```
POST /auth/login  { "username": "admin", "password": "password123" }

GET /bookings/stats?from_date=2026-01-01T00:00:00&to_date=2026-12-31T23:59:59
→ total bookings, revenue, breakdown by status and by day
```

### Useful commands
```bash
docker compose up -d                          # Start in background
docker compose up --build booking-service -d  # Rebuild a single service
docker compose down                           # Stop all containers
docker compose down -v                        # Stop and reset databases
docker compose logs -f booking-service        # Follow logs of a service
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
├── auth-service/           # Spring Boot — Authentication & JWT
├── company-service/        # Spring Boot — Companies & Services catalog
├── booking-service/        # FastAPI — Reservations, state machine, repository pattern
│   ├── app/
│   │   ├── models/
│   │   ├── schemas/
│   │   ├── repositories/   # BookingRepository (data access layer)
│   │   ├── services/       # BookingService (business logic)
│   │   ├── routers/
│   │   ├── state_machine.py
│   │   └── dependencies/
│   └── tests/
├── notification-service/   # Node.js — RabbitMQ consumer, in-app notifications, email
│   └── src/
│       ├── consumers/      # bookingConsumer (handler map pattern)
│       ├── services/       # notificationService, emailService, emailTemplates
│       └── routes/
├── angular-client/         # Angular 18 SPA — booking UI, operator panel, notifications
│   └── src/app/
│       ├── core/           # services, models, guards, enums
│       ├── features/       # home, operator, services pages
│       └── shared/         # navbar, notification-bell, confirmation-dialog components
├── gateway/                # Nginx config
├── docker/                 # DB init script + seed data
├── docker-compose.yml      # Full stack orchestration
└── booking-platform.postman_collection.json
```
