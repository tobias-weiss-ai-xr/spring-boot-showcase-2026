# 04 — Solution Strategy

CropGuard's architecture follows one dominant strategy: **a layered, convention-driven
Spring stack whose package structure doubles as the course index.** Every decision serves
demonstrability first, correctness second, and simplicity throughout.

## The strategy in six points

1. **Layered package structure mirroring the curriculum**
   `controller → service → repository → entity`, plus `dto`, `exception`, `security`,
   `config`, `actuator`, `domain`. A learner can navigate module-by-module: the directory
   layout *is* the syllabus. (Alternative — feature packages — rejected because it would blur
   the course-module mapping, which is the point of the project.)

2. **Spring defaults + minimal ceremony**
   Records for DTOs (no Lombok), constructor injection only, `@RestControllerAdvice` for
   RFC 7807 errors, Actuator for health. Fewer moving parts = easier to teach.

3. **Stateless JWT security with explicit roles**
   `POST /api/auth/login` exchanges credentials for a JWT; an `OncePerRequestFilter` turns
   the Bearer token into an `Authentication`; `@PreAuthorize` enforces
   FARMER-submits / ASSESSOR-assesses. Demonstrates Spring Security without sessions.

4. **Location-based pricing with bundled, deterministic data**
   Premium = `base × bundesland.riskFactor × droughtAdjustment × deductible.premiumFactor`.
   The DWD 1 km drought grid is bundled as a static `.asc.gz` snapshot and loaded into memory
   at startup — no runtime network dependency, reproducible quotes. Going live would swap
   the data source behind the same service.

5. **Exploit the demo environment where honest**
   H2 in-memory + `DataInitializer` reseeds one of each entity on boot, so every endpoint
   works immediately. Cost: data is lost on restart — acceptable and *documented*.

6. **A real test pyramid + browser E2E**
   42 backend tests (unit/slice/integration), 2 Karma component tests, 12 Playwright
   end-to-end scenarios that prove real user journeys (login, quoting, plot→policy→claim,
   assess workflow, role guards). E2E tests seed their own unique data through the REST API
   to stay deterministic against the shared dev database.

7. **Insurance-core patterns over hardcoded product data (Sapiens alignment)**
   Rating values live in loader-validated YAML packs (`rating/default.yml` + country
   override `rating/pl.yml`) instead of enum constants — a new tariff is a config entry,
   not a redeploy. The core is split into `modules/{policy,billing,claims}`
   (PolicyMaster/BillingMaster/ClaimsMaster pattern) with a boundary test, the OpenAPI
   document is a tested contract, and a config-gated legacy CSV import demonstrates
   Bestands-Migration. See README "Sapiens Mapping".

## Resulting shape

```
cropguard/                      # backend (Spring Boot 3.3.5, Java 21, H2)
├── controller/  dto/  service/  repository/  entity/
├── modules/{policy,billing,claims}/   # Sapiens-style core modules + boundary test
├── domain/  exception/  security/  config/  actuator/
├── resources/rating/*.yml            # rating packs (default + country override)
└── src/test/ …                    # pyramid: @SpringBootTest, @WebMvcTest, @DataJpaTest, Mockito
frontend/                       # Angular 20 SPA (standalone, plain CSS)
└── e2e/                         # Playwright suite (POMs, API fixtures, HTML report)
```
