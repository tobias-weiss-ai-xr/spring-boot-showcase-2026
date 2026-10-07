# 02 — Constraints

## Technical Constraints

| Constraint | Value | Rationale |
|------------|-------|-----------|
| Language | **Java 21** (class file v65) | Chosen by the course; LTS line with records/patterns. |
| Backend framework | **Spring Boot 3.3.5** | Course version; gives auto-configuration for every module. |
| Persistence | **H2 in-memory**, JPA/Hibernate `create-drop`, H2 console | Zero-install demo; data is re-seeded on every boot. |
| Frontend | **Angular 20**, standalone components, no NgModules | Latest stable compatible with the dev node (v22). |
| AuthN/AuthZ | **JWT (jjwt 0.12.6)**, BCrypt, method security | Stateless demo of Spring Security; dev secret only. |
| API error model | **RFC 7807 `ProblemDetail`** | Course module 6 requirement. |
| Build | **Maven 3.9**, `spring-boot-maven-plugin`; npm for the frontend | Standard tooling. |

## Organizational Constraints

- Single developer / single repository; the project is also the **teaching artifact**.
- No production environment exists; "deployment" means a local demo run.
- The frontend lives in the same repository under `frontend/` (previously decided backend-only;
  the Angular SPA was added on request).
- The codebase **is deliberately a demo**: monetization, real security hygiene, and scaling
  are explicitly out of scope.

## Conventions (team/architecture standards)

| Convention | Where enforced |
|------------|----------------|
| Constructor injection only (no field injection) | All services/controllers |
| DTOs are Java **records** with Bean Validation annotations | `dto/` package |
| Errors via `@RestControllerAdvice` → RFC 7807 `ProblemDetail` | `exception/` package |
| REST: `@RestController`, correct status codes (201/200/404/400/403) | `controller/` package |
| Role checks via `@PreAuthorize` | controllers |
| German domain language where possible (Bundesländer, Schäden) | domain/enums |

## Compliance Constraints

None beyond the course curriculum. The DWD-style drought-index grid bundled under
`src/main/resources/dwd/` is a **demo asset** (ESRI ASCII grid format, 1 km cells); no
software license is asserted for it — replace or license it properly before any redistribution.
