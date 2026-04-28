# Checkpoints & delivery log — Unified Service Scheduler (Monolith)

This document describes how the application was built **in a single Spring Boot codebase** (monolith), aligned with `cursor-stategy.md`, and is suitable to share in interviews and with hiring teams.

---

## Architecture & principles

| Checkpoint | Summary |
|------------|---------|
| **CP-0 — Monolith** | Auth, customer/vehicle, dealership/resources, appointments, `appointment_slots` projection, and notification stub live in **one JVM** and **one PostgreSQL** DB; in-process calls instead of Feign across services. |
| **CP-1 — Data** | Flyway `V1__auth_and_core.sql`: tables for users, refresh_tokens, customers, vehicles, dealerships, service_types, service_bays, technicians, schedules, blocked_slots, appointments, audit, sequence `booking_ref_seq`, projection table. |
| **CP-2 — Security** | JWT **RS256** (Nimbus): `JwtTokenProvider` signs access tokens; `spring-boot-starter-oauth2-resource-server` plus custom `JwtDecoder` (`JwtBlacklistAwareDecoder`) validates **`jti`** against a **Redis** blacklist; RBAC via `@EnableMethodSecurity` and `@PreAuthorize`. |
| **CP-3 — Headers & internal** | `SecurityHeadersFilter` adds baseline security headers; `InternalSecretFilter` protects `/internal/**` with **`X-Internal-Secret`**. |
| **CP-4 — Booking** | `AppointmentOrchestrationService`: validates vehicle ownership, Redis distributed lock (`RedisLockService`), assigns bay/technician via `AvailabilityService`, generates `APT-{year}-{6-digit}` via `booking_ref_seq`. |
| **CP-5 — Post-commit events** | `AppointmentCommittedListener` (`@TransactionalEventListener(AFTER_COMMIT)`): writes projection rows and logs a notification stub (Kafka could replace the log line later). |
| **CP-6 — REST** | `/api/v1/auth/*`, `/internal/vehicles/...`, `/api/v1/customers/me`, `/api/v1/vehicles`, `/api/v1/dealerships`, `/api/v1/service-types`, `/api/v1/availability`, `/api/v1/appointments`, OpenAPI Swagger UI. |
| **CP-7 — Dev experience** | `local` profile + `LocalDataSeeder`: demo user (`customer@keyloop.local` / `DemoPass123!`), dealership, services, bays, technician + working-hours schedule. |

---

## Delivery timeline (technical)

1. Standardize `pom.xml` (Spring Boot 4, JPA, Flyway + PostgreSQL, Redis, OAuth2 JWT, Validation, Actuator, Micrometer tracing, SpringDoc, Testcontainers where applicable).
2. Design Flyway migrations and entities against a unified schema.
3. Implement security: RSA JWT, Redis blacklist, internal filter, method security.
4. Implement availability (overlapping appointments + blocked slots + technician schedules).
5. Complete booking + cancellation flows (customer cancel **within 24h of start** blocked per spec).
6. Fill in REST endpoints + OpenAPI.
7. Seeder + `docker-compose` for PostgreSQL/Redis + this checkpoint document.

---

## Quick run (local)

```bash
docker compose up -d
./mvnw spring-boot:run
```

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health: `http://localhost:8080/actuator/health`
- JWKS: `GET /api/v1/auth/.well-known/jwks.json`

After seed: call `POST /api/v1/auth/login` with the demo user, use the Bearer token, then try `GET /api/v1/availability` and `POST /api/v1/appointments`.

---

## Notes for hiring managers

- **Monolith by design**: enough domain and security behavior for a solid demo and easy onboarding; split into microservices later by extracting modules and a broker if needed.
- **Kafka / gateway / Eureka** from a “distributed” blueprint are represented here by **domain events + listeners** (and logs) to keep local infrastructure light; package boundaries remain clear for a future migration.
- **Redis** backs booking locks and JWT blacklisting — a compact production-style pattern inside one process.

---

*Implemented directly in the `interview` repository as requested — no separate project scaffold.*
