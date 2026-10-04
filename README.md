# CropGuard — Crop Insurance Management System

Domain-specific Spring Boot demo: agricultural hail insurance for the German market.
Every module of the [Spring Boot course](https://courses.graphwiz.ai/springboot) at
CertPrep maps 1:1 to a layer of this codebase.

## Domain

Farmers insure plots (Felder) against hail damage. After a hailstorm, they file
claims (Schaeden) which assessors (Gutachter) review, calculate payouts, and approve.

- **Bundesland-based risk zones** — 16 German federal states with DWD-based hail
  risk factors (Bayern 1.5x, Hessen 1.0x baseline, Schleswig-Holstein 0.8x)
- **Crop type catalog** — 14 crops from WHEAT (45 EUR/ha) to HOPS (350 EUR/ha)
- **Premium calculation** — `base × hectares × bundesland.riskFactor × deductible.premiumFactor`
- **Claim workflow** — SUBMITTED → ASSESSED → APPROVED/REJECTED with payout calculation
- **Hail events** — batch linking of claims to registered storms

## Quick Start

```bash
mvn spring-boot:run
```

App starts at http://localhost:8080. H2 console at `/h2-console` (JDBC URL:
`jdbc:h2:mem:cropguard`, user: `sa`, empty password).

## API Tour

Register a farmer and get a JWT:

```bash
curl -X POST http://localhost:8080/api/insureds \
  -H "Content-Type: application/json" \
  -d '{"name":"Max Mustermann","email":"max@farm.de","password":"geheim123","role":"FARMER","bundesland":"HESSEN"}'
```

Quote a policy for 25 ha wheat in Bayern:

```bash
curl "http://localhost:8080/api/policies/quote?cropType=WHEAT&hectares=25&bundesland=BAYERN&deductible=TEN_PERCENT&coverageEur=25000"
```

Create a policy:

```bash
curl -X POST http://localhost:8080/api/policies \
  -H "Content-Type: application/json" \
  -d '{"coverageEur":25000,"deductible":"TEN_PERCENT","status":"ACTIVE","coverageStart":"2026-03-01","coverageEnd":"2026-12-31","plotId":1}'
```

File a claim after a hailstorm:

```bash
curl -X POST http://localhost:8080/api/claims \
  -H "Content-Type: application/json" \
  -d '{"damageDate":"2026-07-15","damageDescription":"Vollstaendiger Ernteverlust durch Hagel","policyId":1}'
```

Actuator health (includes custom `DatabaseHealthIndicator`):

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
| 8. Security | `security/` + `config/SecurityConfig` — JWT, @PreAuthorize |
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

Run with `mvn test`. 13 tests, all passing.

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
└── service/                     # Business logic (M2)
```
