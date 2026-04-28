# Unified Service Scheduler (Monolith)

A **dealership service appointment** app: REST APIs, **JWT (RS256)**, role-based access, **service bay + technician** availability, **Redis** distributed locking, and projection updates **after commit**. A single **Spring Boot** codebase — suited for demos and interviews.

Further reading: [`cursor-stategy.md`](cursor-stategy.md) · [`CHECKPOINTS_DEVELOPMENT.md`](CHECKPOINTS_DEVELOPMENT.md) · [`prompt-stategy.md`](prompt-stategy.md)

---

## Prerequisites

| Requirement | Version / notes |
|-------------|----------------|
| Java | **21** |
| Maven | 3.9+ (or use `./mvnw` in the repo) |
| Docker | Recommended for local PostgreSQL + Redis |

---

## Quick start (local)

### 1. Start PostgreSQL and Redis

From the project root:

```bash
docker compose up -d
```

Defaults:

- **PostgreSQL:** port `5432`, database `scheduler_db`, user `scheduler_user`, password `scheduler_pass` (matches `application.yml`).
- **Redis:** port `6379` (no password).

### 2. (Optional) Environment variables

Copy `.env.example` to `.env` and adjust if needed (DB URL, Redis, `INTERNAL_API_SECRET` for internal APIs).

### 3. Build and run

```bash
./mvnw clean package -DskipTests
./mvnw spring-boot:run
```

On Windows (PowerShell):

```powershell
.\mvnw.cmd spring-boot:run
```

Default Spring profile: **`local`** — Flyway runs migrations and **`LocalDataSeeder`** inserts demo data when the database is empty.

### 4. Smoke checks

| What | URL |
|------|-----|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| Health | http://localhost:8080/actuator/health |
| JWKS | `GET` http://localhost:8080/api/v1/auth/.well-known/jwks.json |

---

## Demo accounts (after seed)

| Role | Email | Password |
|------|-------|----------|
| Customer | `customer@keyloop.local` | `DemoPass123!` |
| Advisor | `advisor@keyloop.local` | `DemoPass123!` |
| Admin | `admin@keyloop.local` | `DemoPass123!` |

Sign in with `POST /api/v1/auth/login`, then send `Authorization: Bearer <accessToken>` on protected endpoints.

---

## Internal APIs (service-to-service style)

Routes under `/internal/**` require:

```http
X-Internal-Secret: <INTERNAL_API_SECRET value>
```

Default in dev: match `app.internal.secret` in `application.yml` / `.env.example`.

---

## Tests

Docker is not required for the default test context (profile `test` uses H2; Redis auto-configuration is disabled there).

```bash
./mvnw test
```

---

## Package layout (high level)

```
src/main/java/com/keyloop/interview/
├── appointment/      # booking, post-commit listener
├── auth/             # JWT, users, refresh tokens
├── customer/         # customers, vehicles, internal owner-check
├── dealership/       # catalog, availability
├── security/         # SecurityConfig, JWT, filters
├── config/           # OpenAPI, properties
├── dev/              # local seed
└── infrastructure/   # Redis lock
```

---

## License

Sample interview project — adapt to your company policy if you reuse it.
