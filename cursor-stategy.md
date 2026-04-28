# Unified Service Scheduler — Implementation Strategy (Monolith)

> **Purpose:** Describe what is **actually implemented** in this repo (`interview`): a **single Spring Boot monolith**, not a multi-repo microservices layout.  
> **Audience:** Hiring managers, reviewers, and engineers who need scope, trade-offs, and delivery quality at a glance.

**Version:** 2.0 (Monolith — aligned with current code, April 2026)

---

## 1. Elevator pitch

An **automotive service appointment** system: customers, advisors, and admins use REST APIs; **JWT RS256** plus **RBAC**; booking validates **service bay** and **technician** availability, uses a **Redis-backed distributed lock**, and generates references `APT-{year}-{6 digits}`; a read-model **`appointment_slots`** projection is updated **after commit**; **Swagger**, **Flyway**, and **Actuator** are included.  
The codebase is organized **by domain in one JVM** for fast demos and interviews, with **clear layering** (controller → service → repository) so modules can be extracted later if needed.

---

## 2. Actual architecture (monolith)

```
                    ┌─────────────────────────────────────────┐
                    │     Spring Boot 4 · single artefact     │
                    │  com.keyloop.interview.*                │
                    └─────────────────────┬───────────────────┘
                                          │
       ┌──────────────┬──────────────┬───────────────┬────────────────┐
       ▼              ▼              ▼               ▼                ▼
   auth/rest     customer/rest   dealership/*   appointment/*   infrastructure
   JWT + JWKS    vehicles        catalog &        orchestration    Redis lock /
   endpoints     internal API    availability                        JWT blacklist
                                          │
                     ┌────────────────────┼────────────────────────┐
                     ▼                    ▼                        ▼
              PostgreSQL (single DB)   Redis (locks, jti TLL)   Domain events →
              Flyway migrations        optional in tests        AFTER_COMMIT listener
```

**Difference vs. a full microservices blueprint:** no Eureka, API Gateway, or Kafka in this repo; **one PostgreSQL database**; **in-process service calls** instead of Feign across processes.

---

## 3. Business context

| Element | Description |
|---------|-------------|
| **Actors** | Customer, Service Advisor, Admin |
| **Main flow** | Pick vehicle + service type + dealership + time window → check bay + technician for the full service duration → **CONFIRMED** or suggest slots via the availability API. |
| **Main packages** | `auth`, `customer`, `dealership`, `appointment`, `security`, `config`, `dev` (local seed). |

---

## 4. Technology stack (maps to `pom.xml`)

| Area | In use |
|------|--------|
| Runtime | Java 21, Spring Boot **4.0.x** |
| Web & API | `spring-boot-starter-webmvc`, **SpringDoc OpenAPI** (Swagger UI) |
| Persistence | Spring Data JPA + **PostgreSQL**, **Flyway** |
| Security | Spring Security + **OAuth2 Resource Server** (JWT validation), tokens signed with **RS256** (**Nimbus JOSE JWT**) |
| Concurrency / cache | Spring Data Redis — **TTL lock** for booking, **`jti` blacklist** (when Redis is absent, code paths degrade safely for CI tests) |
| Observability | Actuator (Prometheus-ready), Micrometer tracing bridge |
| Tests | Spring Boot Test, **H2** (`test` profile), Testcontainers on classpath (optional if Docker is available) |

---

## 5. Data model

- **One** PostgreSQL schema (not physical database-per-service).  
- **Flyway:** `src/main/resources/db/migration/` (unified schema: auth, CRM, dealership resources, appointments, slots projection, audit).  
- **Booking reference:** sequence `booking_ref_seq`; format `APT-{year}-{6 digits}`.

---

## 6. Security

| Concern | Implementation |
|---------|----------------|
| **Authentication** | Register / login / refresh (refresh token hashed in DB); access token JWT RS256 with role and optional `dealership_id` claims |
| **Authorization** | `@PreAuthorize` + `AppointmentAccessSecurity` (owner / same-dealership advisor / admin) |
| **Logout / revoke** | Blacklist `jti` in Redis until token expiry |
| **Internal APIs** | `/internal/**` protected by **`X-Internal-Secret`** (`InternalSecretFilter`) |
| **Headers** | `SecurityHeadersFilter` (e.g. `X-Content-Type-Options`, CSP baseline) |

---

## 7. REST surface (main groups)

| Group | Examples |
|-------|----------|
| Auth | `POST /api/v1/auth/register`, `login`, `refresh`; `GET /api/v1/auth/.well-known/jwks.json` |
| Customer & vehicles | `GET /api/v1/customers/me` (CUSTOMER); CRUD under `/api/v1/vehicles` for owner |
| Catalog | `GET /api/v1/dealerships`, `/service-types`, `/availability?...` |
| Appointments | `POST /api/v1/appointments`; list/detail; `PATCH /api/v1/appointments/{id}/cancel` (customer: **cannot cancel within 24h of start**) |
| Internal | `GET /internal/vehicles/{id}/owner-check?customerId=` |

**Swagger UI:** `/swagger-ui.html` (see `OpenApiConfig`).

---

## 8. Booking flow (as implemented)

`AppointmentOrchestrationService`:

1. Enforce ownership / role rules (customer must own the vehicle; advisor/admin rules as coded).  
2. `AvailabilityService` picks bay + technician without overlapping appointments, blocked slots, or outside working hours.  
3. **`RedisLockService.acquire(dealership + hour bucket)`** reduces double-booking.  
4. Persist appointment; publish **`AppointmentCommittedEvent`**; **`AppointmentCommittedListener`** runs **AFTER_COMMIT** to write `appointment_slots` and log a notification stub.

---

## 9. How to run (for reviewers)

```bash
docker compose up -d    # PostgreSQL + Redis
./mvnw spring-boot:run  # default profile `local` + demo seed
```

- **Swagger:** http://localhost:8080/swagger-ui.html  
- **Demo credentials and IDs:** see **`CHECKPOINTS_DEVELOPMENT.md`**.  

Attach **`CHECKPOINTS_DEVELOPMENT.md`** with this file for a complete “what was built and why” narrative.

---

## 10. Mapping from a “full microservices” spec

| Original idea (distributed) | This monolith |
|----------------------------|---------------|
| Many services + Eureka | **Single** deployable |
| Kafka | **`ApplicationEvent`** + **`@TransactionalEventListener(AFTER_COMMIT)`**; Kafka can be plugged in later |
| API Gateway + global rate limit | Direct port **8080**; rate limiting can be added via filter or edge proxy |
| Database per service | **One** PostgreSQL database |
| Feign between services | **Direct** `@Service` calls in the same JVM |

This is a **deliberate** trade-off for demo velocity and interview clarity, not an accidental omission.

---

## 11. Natural next steps (one-liner for interviews)

Extract bounded contexts into modules or services; replace the in-process listener with a **Kafka** producer; keep JWT validation and Redis patterns as stable contracts at the edge.

---

*This document matches the current `src/main/java/com/keyloop/interview/` layout and configuration.*
