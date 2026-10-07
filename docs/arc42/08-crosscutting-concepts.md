# 08 — Cross-cutting Concepts

Concepts that apply to the whole system.

## 1. Security *(course module 8)*

- **Stateless JWT**: `POST /api/auth/login` validates against the `insureds` table
  (`BCryptPasswordEncoder.matches`) and returns a signed HS256/HS512 JWT
  (`JwtService.generateToken(subject=email, role)`).
- **Filter chain**: `SecurityConfig` — CSRF off, `STATELESS`, permits `/api/auth/**`,
  `/api/insureds/**`, `/h2-console/**`, `/actuator/**`; `JwtAuthFilter` runs before
  `UsernamePasswordAuthenticationFilter` and populates the `SecurityContext` with
  `ROLE_<role>`.
- **Authorization**: method security (`@EnableMethodSecurity`) + `@PreAuthorize(
  "hasRole('ASSESSOR')")` on the assess endpoint. Route guards on the frontend mirror this.
- **Passwords**: BCrypt-hashed; the hash is never serialized (`@JsonIgnore`).
- **Known dev-only shortcuts**: hardcoded secret in `application.yml`, token TTL 86400 s —
  explicitly non-production (ADR-002).

## 2. Error Handling *(module 6)*

- `GlobalExceptionHandler` (`@RestControllerAdvice`) maps every failure to **RFC 7807
  `ProblemDetail`**:

| Exception | HTTP | Title |
|-----------|------|-------|
| `ResourceNotFoundException` | 404 | Resource Not Found |
| `BusinessException` | 400 | Business Rule Violation |
| `UnauthorizedException` | 401 | Unauthorized |
| `MethodArgumentNotValidException` | 400 | Validation Error (field details) |
| `IllegalArgumentException` | 400 | Invalid Argument |

The frontend reads `problem.detail` and shows it in `role="alert"` boxes.

## 3. Validation *(module 4)*

- DTOs are records with annotations: `@NotBlank`, `@NotNull`, `@Email`, `@Min`, `@Size`,
  `@Min/@Max`. Controllers apply `@Valid`. Codes 400 with field-level detail.

## 4. Persistence *(module 5)*

- JPA entities (`@Entity`), `@ManyToOne` associations are **EAGER** (ARM decision —
  see ADR-005) because `open-in-view: false` and entity serialization otherwise crash with
  lazy proxies. No bidirectional collections in the JSON graph → no recursion.
- Repositories are `JpaRepository` interfaces with derived queries
  (`findByPolicyPlotInsuredId`, `findBySeverity`, …).

## 5. REST / API conventions *(module 3)*

- Resources: `/api/insureds`, `/api/auth`, `/api/plots`, `/api/policies`, `/api/claims`,
  `/api/hail-events`.
- Status codes: 201 for creation, 200 for reads/updates, 400/404/401/403 for errors.
- Query filters (`insuredId`, `severity`, date range, …) as query params; all-list variants
  used by the assessor view.

## 6. Configuration & Profiles *(module 9)*

- `AppProperties` (`@ConfigurationProperties(prefix = "cropguard")`) — JWT secret/TTL, app name.
- Two profiles (`application.yml` / `application-dev.yml`); dev profile logs SQL + Security at DEBUG.

## 7. Observability *(module 10)*

- Actuator exposes `health, info, metrics` with `show-details: always`.
- Custom `DatabaseHealthIndicator` executes a `SELECT COUNT` against each table and reports
  the counts (e.g. `insureds: 2`).

## 8. Frontend cross-cutting

- **Session**: `AuthService` keeps a `signal<LoginResponse|null>`; persisted in `localStorage`.
- **Auth interceptor**: attaches `Authorization: Bearer …`; on 401 logs out and redirects to `/login`.
- **Route guards**: `farmerGuard` / `assessorGuard` build a URL tree (`/farmer`, `/assessor`, `/forbidden`).
- **UI language**: German labels (target audience); plain CSS design system (`.card`, `.badge`,
  `.alert`) — no UI framework dependency.

## 9. Testing *(module 7)*

| Level | Tooling | Coverage |
|-------|---------|----------|
| Backend unit | JUnit 5 + Mockito | PolicyService, ClaimService, DwdRiskGridService |
| Backend slices | `@WebMvcTest` (Auth/Claim/Policy controllers), `@DataJpaTest` (PolicyRepository) | HTTP contracts, validation, security |
| Backend integration | `@SpringBootTest` | context loads |
| Frontend unit | Karma/ChromeHeadless | AuthService (login request + session signals) |
| Frontend E2E | Playwright (POMs, API fixtures) | login/register, role guards, quote, plot→policy→claim, assess workflow |
