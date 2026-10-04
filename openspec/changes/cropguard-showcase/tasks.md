# Implementation Tasks

## Task 1: Services — PolicyService, ClaimService, HailEventService
**Scope:** `src/main/java/com/example/cropguard/service/{PolicyService,ClaimService,HailEventService}.java`
**Accept:** `mvn compile -q`
Create PolicyService (premium calc, coverage validation), ClaimService (submit/assess workflow, status machine), HailEventService (register, find by date/severity, link claims).

## Task 2: Controllers — ClaimController, HailEventController
**Scope:** `src/main/java/com/example/cropguard/controller/{Claim,HailEvent}Controller.java`
**Accept:** `mvn compile -q`
REST endpoints: POST /api/claims, GET /api/claims/{id}, PUT /api/claims/{id}/assess (@PreAuthorize ASSESSOR), GET /api/claims?insuredId=, GET /api/hail-events, GET /api/hail-events/{id}, GET /api/hail-events/{id}/claims.

## Task 3: Security — JWT auth, SecurityConfig, AppProperties
**Scope:** `src/main/java/com/example/cropguard/{security,config}/*.java`
**Accept:** `mvn compile -q`
JwtService (generate/validate tokens), JwtAuthFilter (extract Bearer, set SecurityContext), SecurityConfig (stateless, BCrypt, method security, permit /api/insureds POST), AppProperties record.

## Task 4: Actuator, DataInitializer, application.yml
**Scope:** `src/main/java/com/example/cropguard/{actuator,DataInitializer}.java`, `src/main/resources/*.yml`
**Accept:** `mvn compile -q`
DatabaseHealthIndicator (SELECT COUNT), DataInitializer (seed: 1 farmer, 1 assessor, 1 plot, 1 policy, 1 claim, 1 hail event), application.yml (H2, JPA create-drop, actuator, JWT dev secret), application-dev.yml.

## Task 5: Tests — unit, slice, integration
**Scope:** `src/test/java/com/example/cropguard/**/*.java`
**Accept:** `mvn test -q`
CropGuardApplicationTest (@SpringBootTest contextLoads), PolicyServiceTest (Mockito: create, premium validation), ClaimServiceTest (Mockito: submit on active/expired, assess), ClaimControllerTest (@WebMvcTest: create, 400, 403 for FARMER), PolicyControllerTest (@WebMvcTest: create, negative coverage 400), PolicyRepositoryTest (@DataJpaTest: save, findByStatus).

## Task 6: README
**Scope:** `README.md`
**Accept:** `test -f README.md`
Quick start, API tour (curl examples), course-module mapping table, test pyramid, project structure.
