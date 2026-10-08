# Implementation Tasks

## Task SA-1: Rating packs (Smart Packs + country layer)
**Scope:** `src/main/java/com/example/cropguard/service/RatingPackService.java`, `src/main/resources/rating/*.yml`, `src/main/java/com/example/cropguard/domain/{CropType,Bundesland,Deductible}.java`, `src/main/java/com/example/cropguard/service/PremiumCalculator.java`, `src/main/java/com/example/cropguard/config/AppProperties.java`, `src/test/java/**/RatingPackServiceTest.java`
**Accept:** `mvn test -q`
Externalise rating values (crop base rates/risk categories, Bundesland factors, deductible factors) into loader-validated YAML rating packs resolved by RatingPackService; keep enum constants as catalog keys without numeric ratings; add default pack + at least one override pack (e.g. PL); validate unknown keys/non-positive values fail-fast on startup; wire PremiumCalculator/quote to resolve via the service; add RatingPackServiceTest covering default lookup, invalid pack fail-fast, and country override. All existing tests stay green.

## Task SA-2: Module boundaries policy/billing/claims
**Scope:** `src/main/java/com/example/cropguard/modules/**`, `src/main/java/com/example/cropguard/{controller,service,repository}/**`, `src/test/java/**/ModuleBoundaryTest.java`
**Accept:** `mvn test -q`
Introduce `com.example.cropguard.modules.{policy,billing,claims}` housing module-owned service/repository logic (billing = premium/quote), keep controllers delegating into modules, preserve layer mapping. Add an architectural boundary test asserting modules do not directly access another module's repositories/entities except via an explicit allowance (facade/port). All existing endpoint tests stay green (same status codes/payloads for quote, claims submit, assess).

## Task SA-3: OpenAPI contract test (ACE pattern)
**Scope:** `src/main/java/com/example/cropguard/config/OpenApiConfig.java`, `application*.yml` (springdoc), `src/test/java/**/OpenApiContractTest.java`
**Accept:** `mvn test -q`
Ensure springdoc serves a versioned `GET /v3/api-docs` (OpenAPI 3, info.version from build version), add tags per module, and add OpenApiContractTest verifying the document contains `/api/policies`, `/api/claims`, `/api/plots` and `openapi: 3.x`. (README cross-reference is handled in SA-5.)

## Task SA-4: Legacy data import with validation report
**Scope:** `src/main/java/com/example/cropguard/service/LegacyImportService.java`, `src/main/resources/legacy/ratings_legacy.csv`, `src/main/java/com/example/cropguard/config/AppProperties.java` (`cropguard.legacy-import.enabled`), `application.yml`, `src/test/java/**/LegacyImportServiceTest.java`
**Accept:** `mvn test -q`
Add a config-gated (default off) `CommandLineRunner` that reads the committed legacy CSV fixture, validates every record (unknown/empty/negative values), maps valid rows into rating-pack data, and produces a per-record failure report without aborting startup on invalid rows. LegacyImportServiceTest covers happy path + invalid-row reporting. Document the flag in the README.

## Task SA-5: Sapiens mapping documentation
**Scope:** `README.md`
**Accept:** `grep -qiE 'PolicyMaster' README.md && grep -qiE 'ClaimsMaster' README.md && grep -qiE 'Smart Packs' README.md`
Add a "Sapiens Mapping" section to the README: PolicyMaster≈modules.policy/PolicyService, BillingMaster≈modules.billing (premium/quote), ClaimsMaster≈modules.claims/ClaimService, Smart Packs≈rating-pack YAMLs, country layer≈override packs, ACE≈OpenAPI contract, DigitalSuite≈Angular frontend, DataSuite≈actuator/portfolio data; one sentence each, and a note that Sapiens itself is proprietary (config/integration over source access).

## Task SA-6: Pre-application acceptance sweep
**Scope:** `pom.xml`, `frontend/`, `.github/workflows/ci.yml`, `README.md`
**Accept:** `mvn verify -q && (cd frontend && npm run build 2>/dev/null || true)`
Full build + tests green after all SA tasks; README quick-start still accurate (quote/claim curl tour unchanged); CI still passes. Manual confirmation only — no new feature work.
