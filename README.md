# Microservices Architecture (Backend)

This project implements a booking platform based on a **microservices architecture**, composed of the following components:

- `auth-service` – Authentication, user management, and JWT issuance (Spring Boot + PostgreSQL).
- `company-service` – Management of companies and offered services (Spring Boot + PostgreSQL).
- `booking-service` – Reservation management (FastAPI + PostgreSQL).
- `gateway` – Nginx acting as API Gateway / reverse proxy.

Service-to-service communication is primarily **REST-based**, complemented by **domain events** for specific use cases (e.g., user registration and booking creation).

---

## 1. auth-service

**Technologies:** Spring Boot, Spring Security, JWT, Spring Data JPA, PostgreSQL  

### Scope / Responsibilities

- User registration and authentication.
- JWT token generation and validation.
- Basic user profile management (roles, minimal user data).
- Single source of truth for user identity.

### Data Model (simplified)

- `User`
  - `id`
  - `username`
  - `email`
  - `passwordHash`
  - `role` (e.g., `USER`, `ADMIN`)
  - `createdAt`, `updatedAt`

### REST API (examples)

- `POST /auth/register`
  - Creates a new user.
- `POST /auth/login`
  - Validates credentials and returns a JWT.
- `GET /auth/me`
  - Returns basic information of the authenticated user (based on JWT).

### Communication with Other Services

#### REST

- **JWT provider**: Other services (such as `booking-service`) validate the JWT issued by `auth-service` to identify users.
- Optionally exposes internal endpoints such as:
  - `GET /internal/users/{id}`
  - `GET /internal/users/by-username/{username}`  
  for identity-related lookups if needed.

#### Domain Events

> Event broker implementation to be defined (e.g., RabbitMQ, Kafka, etc.).  
> Described here at an architectural level.

**Published events:**

- `UserRegistered`
  - Payload: `userId`, `username`, `email`, `registeredAt`
  - Potential consumers:
    - Notification services
    - Analytics services

---

## 2. company-service

**Technologies:** Spring Boot, Spring Data JPA, PostgreSQL  

### Scope / Responsibilities

- Management of **companies** offering services (barbershops, sports courts, studios, etc.).
- Management of **services** associated with each company.
- Source of truth for service catalog information (what can be booked).

### Data Model (simplified)

- `Company`
  - `id`
  - `name`
  - `description`
  - `address`
  - `phone`
  - `createdAt`, `updatedAt`

- `Service`
  - `id`
  - `companyId`
  - `name`
  - `description`
  - `durationMinutes`
  - `price`
  - `isActive`
  - `createdAt`, `updatedAt`

### REST API (examples)

- `GET /companies`
  - List available companies.
- `GET /companies/{id}`
  - Company details.
- `GET /companies/{id}/services`
  - List services offered by a company.
- `POST /companies`
- `POST /companies/{id}/services`
- `PUT /services/{id}`
- `DELETE /services/{id}`

### Communication with Other Services

#### REST

- Can be called by `booking-service` to validate:
  - Whether a service exists.
  - Whether a service is active.

Example internal endpoint:
- `GET /internal/services/{serviceId}`

#### Domain Events

**Published events (optional):**

- `ServiceCreated`
- `ServiceUpdated`
- `ServiceDeactivated`

This allows other services (e.g., recommendation, notification) to react to catalog changes without tight coupling.

---

## 3. booking-service

**Technologies:** FastAPI, SQLAlchemy (or chosen ORM), PostgreSQL  

### Scope / Responsibilities

- Management of **reservations** made by users.
- Validation of booking business rules (availability, time constraints, etc.).
- Source of truth for booking history and status.

### Data Model (simplified)

- `Booking`
  - `id`
  - `userId`
  - `serviceId`
  - `companyId` (optional, for query optimization)
  - `startTime`
  - `endTime`
  - `status` (`PENDING`, `CONFIRMED`, `CANCELED`, etc.)
  - `createdAt`, `updatedAt`

### REST API (examples)

- `POST /bookings`
  - Creates a booking for the authenticated user.
- `GET /bookings/my`
  - Lists bookings for the authenticated user.
- `GET /bookings/{id}`
  - Returns booking details (if authorized).
- `PATCH /bookings/{id}/cancel`
  - Cancels a booking.

### Communication with Other Services

#### REST

- **With auth-service**
  - Validates the JWT included in the `Authorization` header.
  - Extracts `userId` from the token (without necessarily calling auth-service each time).

- **With company-service**
  - Validates that the `serviceId` exists and is active when creating a booking.
  - Optionally retrieves service duration/price if not duplicated locally.

Example internal call:
- `GET http://company-service:8082/internal/services/{serviceId}`

#### Domain Events

**Published events:**

- `BookingCreated`
  - Payload: `bookingId`, `userId`, `serviceId`, `companyId`, `startTime`, `status`, `createdAt`
- `BookingCanceled`
  - Payload: `bookingId`, `userId`, `serviceId`, `companyId`, `canceledAt`

**Potential consumers:**

- Notification services (email, messaging).
- Analytics/reporting services.
- External integrations.

---

## 4. gateway (Nginx)

**Technologies:** Nginx

### Scope / Responsibilities

- Acts as an **API Gateway / reverse proxy**.
- Provides a single entry point for external clients (e.g., Angular frontend).
- Routes incoming requests to internal microservices:

  - `/auth/*` → `auth-service`
  - `/companies/*` → `company-service`
  - `/bookings/*` → `booking-service`

- Optionally handles:
  - CORS configuration
  - Request logging
  - Compression
  - Timeouts
  - Static frontend serving (future enhancement)

### Conceptual Configuration Example

```nginx
server {
    listen 80;

    location /auth/ {
        proxy_pass http://auth-service:8081/;
    }

    location /companies/ {
        proxy_pass http://company-service:8082/;
    }

    location /bookings/ {
        proxy_pass http://booking-service:8000/;
    }
}