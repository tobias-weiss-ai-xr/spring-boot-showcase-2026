# CropGuard — Crop Insurance Management System

Domain-specific Spring Boot demo: agricultural hail insurance for the German market.
Every module of the [Spring Boot course](https://courses.graphwiz.ai/springboot) at
CertPrep maps 1:1 to a layer of this codebase.

## Domain

Farmers insure plots (Felder) against hail damage. After a hailstorm, they file
claims (Schaeden) which assessors (Gutachter) review, calculate payouts, and approve.

- **Bundesland-based risk zones** — 16 German federal states with DWD-based hail
  risk factors (Bayern 1.5x, Hessen 1.0x baseline, Schleswig-Holstein 0.8x)
- **DWD drought index** — 1km grid (July 1991–2020, bundled demo asset) loaded at startup,
  location-specific drought adjustment via GK3 coordinates (factor 0.95–1.4)
- **Crop type catalog** — 14 crops from WHEAT (45 EUR/ha) to HOPS (350 EUR/ha)
- **Premium calculation** — `base × hectares × bundesland.riskFactor × droughtAdjustment × deductible.premiumFactor`
- **Claim workflow** — SUBMITTED → APPROVED/REJECTED via assessor review, with payout
  calculation (the enum also defines UNDER_REVIEW, ASSESSED, PAID, but no API path sets them yet)
- **Hail events** — batch linking of claims to registered storms

## Quick Start

```bash
mvn spring-boot:run
```

App starts at http://localhost:8080. H2 console at `/h2-console` (JDBC URL:
`jdbc:h2:mem:cropguard`, user: `sa`, empty password).

Seeded users (created by `DataInitializer`):

| Email | Password | Role |
|-------|----------|------|
| `max@bauernhof.de` | `passwort123` | FARMER |
| `lisa@cropguard.de` | `assessor123` | ASSESSOR |

## API Tour

Register a farmer:

```bash
curl -X POST http://localhost:8080/api/insureds \
  -H "Content-Type: application/json" \
  -d '{"name":"Max Mustermann","email":"max@farm.de","password":"geheim123","role":"FARMER","bundesland":"HESSEN"}'
```

Log in and capture the JWT (role-based endpoints require it):

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"max@farm.de","password":"geheim123"}' | python -c "import sys,json; print(json.load(sys.stdin)['token'])")
```

Quote a policy for 25 ha wheat in Bayern (requires token):

```bash
curl "http://localhost:8080/api/policies/quote?cropType=WHEAT&hectares=25&bundesland=BAYERN&deductible=TEN_PERCENT&coverageEur=25000&coordinateE=3700000&coordinateN=5570000" \
  -H "Authorization: Bearer $TOKEN"
```

Create a policy:

```bash
curl -X POST http://localhost:8080/api/policies \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"coverageEur":25000,"deductible":"TEN_PERCENT","status":"ACTIVE","coverageStart":"2026-03-01","coverageEnd":"2026-12-31","plotId":1}'
```

File a claim after a hailstorm:

```bash
curl -X POST http://localhost:8080/api/claims \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"damageDate":"2026-07-15","damageDescription":"Vollstaendiger Ernteverlust durch Hagel","policyId":1}'
```

Assess a claim — FARMER is forbidden (403), so log in as the seeded assessor:

```bash
ASSESSOR_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"lisa@cropguard.de","password":"assessor123"}' | python -c "import sys,json; print(json.load(sys.stdin)['token'])")

curl -X PUT http://localhost:8080/api/claims/1/assess \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ASSESSOR_TOKEN" \
  -d '{"damagePercent":80,"decision":"APPROVED","assessorNotes":"Total loss"}'
```

Actuator health (includes custom `DatabaseHealthIndicator`, public):

```bash
curl http://localhost:8080/actuator/health
```

## Course Module Mapping

| Module | Where in this project |
|--------|----------------------|
| 1. Getting Started | `pom.xml` — starters bring auto-configuration |
| 2. Spring Core / DI | All services use constructor injection |
| 3. REST APIs | `controller/` — correct HTTP status codes |
| 4. Validation | `dto/` — records with Bean Validation annotations |
| 5. Spring Data JPA | `entity/` + `repository/` — derived query methods |
| 6. Error Handling | `exception/GlobalExceptionHandler` — RFC 7807 ProblemDetail |
| 7. Testing | `src/test/` — test pyramid (unit, slice, integration) |
| 8. Security | `security/` + `config/SecurityConfig` — JWT login (`/api/auth/login`), @PreAuthorize |
| 9. Configuration | `config/AppProperties` + profiles (`application-dev.yml`) |
| 10. Actuator | `actuator/DatabaseHealthIndicator` + `/actuator/health` |

## Test Pyramid

```
        ┌────────────┐
        │ Integration │  @SpringBootTest — context loads
        ├────────────┤
        │   Slices   │  @WebMvcTest (controllers), @DataJpaTest (repository)
        ├────────────┤
        │    Unit    │  Mockito — PolicyServiceTest, ClaimServiceTest
        └────────────┘
```

Run with `mvn test`. 23 tests, all passing.

## Frontend (Angular)

A standalone Angular SPA in [`frontend/`](frontend/) talks to the backend through a dev proxy
(no CORS setup needed). Requires Node ≥ 22.

```bash
# Terminal 1 — backend
mvn spring-boot:run        # :8080

# Terminal 2 — frontend
cd frontend
npm install
npm start                  # :4200 → proxies /api and /actuator to :8080
```

Open http://localhost:4200 and log in as the seeded FARMER or ASSESSOR.

- **FARMER view** — live premium quote (with DWD drought adjustment), register plot → buy policy →
  file a claim, and lists of own policies/claims
- **ASSESSOR view** — full claim queue with status filter, assess/approve/reject form, hail events
- **Risk map** — canvas heat map of the DWD drought index grid with plot markers and hover
  tooltip, shown on both dashboards (`GET /api/risk-map`)
- JWT is stored in `localStorage`; an HTTP interceptor attaches it, guards route by role, and a 401
  returns you to the login page

Run the frontend tests with `npm test` (Karma/ChromeHeadless) and the end-to-end suite with `npm run test:e2e` (Playwright; auto-starts both servers, seeds its own data, writes an HTML report you can open with `npm run test:e2e:report`). 11 e2e specs cover login/registration, role guards, quote recalc, the full plot→policy→claim lifecycle, the assessor claim workflow, and the DWD risk map.

## Architecture Documentation

The software architecture is documented following the [arc42](https://arc42.org) template with
full [C4 diagrams](https://c4model.com) (system context, containers, components, code) — see
[`docs/arc42/`](docs/arc42/README.md).

## Project Structure

```
src/main/java/com/example/cropguard/
├── CropGuardApplication.java   # Main class
├── actuator/                    # Custom health indicators (M10)
├── config/                      # SecurityConfig, AppProperties (M8, M9)
├── controller/                  # REST endpoints (M3)
├── domain/                      # Enums: Bundesland, CropType, Deductible
├── dto/                         # Records with validation (M4)
├── entity/                      # JPA entities (M5)
├── exception/                   # RFC 7807 error handling (M6)
├── repository/                  # Spring Data JPA (M5)
├── security/                    # JWT auth (M8)
└── service/                     # Business logic (M2), DwdRiskGridService (DWD grid)
```
