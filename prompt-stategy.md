# Short prompt — Unified Service Scheduler (Monolith · Keyloop interview)

**Use for:** Cursor / other AI or new developers — **must match the current codebase**.

---

## Directive

Build and maintain **one Spring Boot application** (monolith under `com.keyloop.interview`). **Do not** generate multiple deployable services in this repo unless explicitly asked to split repositories.

Context: **automotive service booking** — customer / advisor / admin; PostgreSQL + Redis (tests may use H2 and optional Redis).

---

## Stack and constraints

| Topic | Value |
|-------|--------|
| Runtime | Spring Boot **4**, Java **21** |
| API | REST `/api/v1/...`; internal `/internal/...`; **Swagger** |
| Persistence | **JPA + Flyway + PostgreSQL** (`db/migration`) |
| Security | JWT **RS256** (Nimbus), OAuth2 Resource Server, **RBAC** (`@PreAuthorize`) |
| Lock / revoke | Redis: **distributed lock** for booking, **`jti` blacklist** |
| Observability | Actuator (metrics / Prometheus-friendly) |

Do **not** add Eureka, Spring Cloud Gateway, or Kafka **unless** there is a separate issue — default integration is **in-process events** + **AFTER_COMMIT** listeners.

---

## Mental module map (packages)

```
auth.*          JWT (register/login/refresh/logout), JWKS
customer.*      Customer, vehicle, internal owner-check + secret header
dealership.*    Dealership / service type / bay / technician, availability
appointment.*  Booking orchestration, REST, security helpers, events/listener
security.*      SecurityConfig, JWT decoder + blacklist, filters
config.*        JwtProperties, AppProperties, OpenAPI
dev.*           LocalDataSeeder (@Profile local)
```

---

## Editing rules

1. Prefer **small, layered changes** — controllers should not query the DB directly.  
2. **Do not rewrite applied Flyway migrations** for cosmetic refactors.  
3. Test profile: `application-test.yml` — **no Docker required**; Redis is optional in Redis-dependent beans.  
4. Recruiter-facing docs: **`cursor-stategy.md`** + **`CHECKPOINTS_DEVELOPMENT.md`**.

---

## Checklist for a new feature in this monolith

- [ ] Entity / Flyway migration (if schema changes)  
- [ ] Service + tests (context or unit as appropriate)  
- [ ] OpenAPI notes if the endpoint is public  
- [ ] Security: correct roles and paths in `SecurityConfiguration`

---

*This file accompanies the portfolio and describes the **intentional monolith** — not a multi-service swarm.*
