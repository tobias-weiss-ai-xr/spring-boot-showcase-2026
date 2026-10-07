# 08 — Cross-cutting Concepts

Concepts that apply to the whole system.

## 1. Security *(course module 8)*

- **Stateless JWT**: `POST /api/auth/login` validates against the `insureds` table
  (`BCryptPasswordEncoder.matches`) and returns a signed HS512 JWT
  (key derived from the 70-character dev secret; TTL 86400 s = 24 h).
- **Filter chain**: `SecurityConfig` — CSRF off, `STATELESS`, permits `/api/auth/**`,
  `/api/insureds/**`, `/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html`, `/h2-console/**`,
  `/actuator/**`; `JwtAuthFilter` runs before
  `UsernamePasswordAuthenticationFilter` and populates the `SecurityContext` with
  `ROLE_<role>`. The OpenAPI docs are deliberately public in the demo — they disclose the API
  surface only, no data; harden for production (see 11).
- **Authorization**: method security (`@EnableMethodSecurity`) + `@PreAuthorize` on the assess
  (ASSESSOR) and plot-creation (FARMER) endpoints. Route guards on the frontend mirror this.
- **Ownership / tenant scoping**: farmers only ever see and mutate their own plots, policies and
  claims — list endpoints scope by the JWT subject, and create/submit verify that the referenced
  plot/policy belongs to the caller (rejects cross-tenant IDs from the payload). Assessors see
  everything. `assess` accepts only `APPROVED|REJECTED`; the claim status machine is server-owned.
- **Public registration**: `POST /api/insureds` ignores the `role` field and always creates a
  FARMER (no self-registered assessors — assessors exist only via seeding).
- **Unauthenticated API calls** get **401** (via `HttpStatusEntryPoint`), wrong role gets 403.
- **Passwords**: BCrypt-hashed; the hash is never serialized (`@JsonIgnore`).
- **Known dev-only shortcuts**: hardcoded secret in `application.yml`, token TTL 86400 s —
  explicitly non-production (ADR-002).
- **Claim status machine**: `SUBMITTED → APPROVED/REJECTED` via `assess()` (decision is
  whitelist-validated); the enum's `UNDER_REVIEW`/`ASSESSED`/`PAID` are defined but unreachable
  through the current API (see 06 Runtime View). Policies are created server-side as `ACTIVE` —
  clients cannot set statuses.

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
  `/api/hail-events`, `/api/risk-map` (downsampled DWD grid for the SPA heat map).
- Live API reference: springdoc-openapi serves **`/swagger-ui`** and `/v3/api-docs`
  (`OpenApiConfig` provides title/description).
- Status codes: 201 for creation, 200 for reads/updates, 400/404/401/403 for errors.
- Query filters (`insuredId`, `severity`, date range, …) as query params; all-list variants
  used by the assessor view.

## 6. Configuration & Profiles *(module 9)*

- `AppProperties` (`@ConfigurationProperties(prefix = "cropguard")`) — JWT secret/TTL, app name.
- Two profiles (`application.yml` / `application-dev.yml`); dev profile logs SQL + Security at DEBUG.

## 7. Observability *(module 10)*

- Actuator exposes `health, info, metrics` with `show-details: always`.
- Custom `DatabaseHealthIndicator` executes a `SELECT COUNT(*)` on the `insureds` table and
  reports the row count as the `insureds` health detail.

## 8. Frontend cross-cutting

- **Session**: `AuthService` keeps a `signal<LoginResponse|null>`; persisted in `localStorage`.
- **Auth interceptor**: attaches `Authorization: Bearer …`; on 401 logs out and redirects to `/login`.
- **Route guards**: `farmerGuard` / `assessorGuard` build a URL tree (`/farmer`, `/assessor`, `/forbidden`).
- **UI language**: German labels (target audience); `LOCALE_ID = 'de'` — currency, number and
  date pipes render de-DE (`1.125,00 €`, `dd.MM.yyyy`). Plain CSS design system (`.card`,
  `.badge`, `.alert`) — no UI framework dependency.
- **Feedback conventions**: errors inline as `.alert.error` (`role="alert"`); successes inline as
  `.alert.success` (`role="status"`, auto-dismiss after 3 s) — no blocking `window.alert` dialogs.
  Empty tables show a muted hint row; the risk-map canvas carries `role="img"` + German aria-label.
- **Login page** shows the seeded demo accounts (FARMER / ASSESSOR) for quick demo access.

## 9. Testing *(module 7)*

| Level | Tooling | Coverage |
|-------|---------|----------|
| Backend unit | JUnit 5 + Mockito | PolicyService, ClaimService, DwdRiskGridService |
| Backend slices | `@WebMvcTest` (Auth/Claim/Policy controllers), `@DataJpaTest` (PolicyRepository) | HTTP contracts, validation, security |
| Backend integration | `@SpringBootTest` | context loads |
| Frontend unit | Karma/ChromeHeadless | AuthService (login request + session signals) |
| Frontend E2E | Playwright (POMs, API fixtures) | login/register, role guards, quote, plot→policy→claim, assess workflow |
