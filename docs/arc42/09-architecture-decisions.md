# 09 — Architecture Decisions

Each decision names the context, the decision, and the alternatives considered. Decisions are
numbered `ADR-0XX`.

## ADR-001 — DTOs as Java records with Bean Validation

- **Context**: Controllers need request/response shapes that are immutable and validated.
- **Decision**: All DTOs are `record` types in `dto/` with Bean Validation annotations;
  controllers take `@Valid @RequestBody`.
- **Alternative / why not**: Lombok (`@Data`) — adds a dependency and hides boilerplate the
  course wants to teach; mutable POJOs — no benefit in a stateless REST API.

## ADR-002 — Stateless JWT authentication, dev secret

- **Context**: Module 8 requires a demonstrable security story.
- **Decision**: `POST /api/auth/login` → signed JWT; stateless `SecurityConfig` with
  `JwtAuthFilter`; roles enforced by `@PreAuthorize`.
- **Alternative / why not**: OAuth2/Keycloak — correct for production, unteachable as a
  one-file demo; HTTP-Basic — no token demonstration. The secret is hardcoded and documented
  as a **non-goal** (demo only).

## ADR-003 — H2 in-memory database + DataInitializer seeding

- **Context**: The demo must run with zero external setup.
- **Decision**: H2 in-memory, `create-drop`, `CommandLineRunner` seeds one of each entity.
- **Alternative / why not**: PostgreSQL/MySQL — requires installation; Flyway migrations —
  ceremony with no migration history to manage in a demo.
- **Trade-off**: data lost on restart (mitigated by re-seeding).

## ADR-004 — DWD drought data bundled as a static grid (no runtime external API)

- **Context**: Pricing should be location-dependent (drought), but the demo must be offline-capable and deterministic.
- **Decision**: A 1 km ASCII snapshot (`2000`-row/col raster, 1991–2020 July index) is bundled
  and loaded at startup by `DwdRiskGridService`; premium multiplies by the cell's drought factor.
- **Alternative / why not**: live DWD API/WMS — requires network, keys, and introduces flaky
  quotes; server-side geocoding — external dependency. The service boundary is the documented
  upgrade path to a live source.

## ADR-005 — EAGER `@ManyToOne` associations (serialization safety)

- **Context**: `open-in-view: false` + lazy proxies crashed JSON serialization of every
  entity-returning endpoint ("could not initialize proxy — no Session").
- **Decision**: The three `@ManyToOne` associations (Claim→Policy, Policy→Plot, Plot→Insured)
  are `FetchType.EAGER`.
- **Trade-off (known ceiling)**: N+1 query risk as data grows; the *correct* long-term fix is
  response-DTO projection. Chosen because the association graph is small and the API contract
  currently *is* the entity JSON.

## ADR-006 — Angular dev-server proxy instead of backend CORS

- **Context**: The SPA and the API run on different origins in dev.
- **Decision**: `frontend/proxy.conf.json` forwards `/api` and `/actuator` from `:4200` to
  `:8080`; `angular.json` enables `proxyConfig`.
- **Alternative / why not**: CORS filter on the backend — would be needed anyway for
  production, but the proxy keeps the backend contract clean and mirrors how the course shows
  backend-only demos.

## ADR-007 — RFC 7807 `ProblemDetail` error model

- **Context**: Course module 6 + a consistent API contract.
- **Decision**: `@RestControllerAdvice` maps domain exceptions to `ProblemDetail` (400/401/404);
  validation returns field-level detail; the frontend surfaces `detail` in alert boxes.
- **Alternative / why not**: raw exception messages / custom body shapes — no standard, harder
  to consume consistently.

## ADR-008 — Test strategy: pyramid + isolated seeded E2E

- **Context**: Course module 7 plus the desire for a credible quality story.
- **Decision**: Backend: unit + `@WebMvcTest`/`@DataJpaTest` slices + `@SpringBootTest`.
  Frontend: Karma for the auth service; Playwright with **page objects + API-fixture seeding**
  of unique data, single worker, HTML report.
- **Alternative / why not**: E2E-only — slow, flaky; playing back against a reset database —
  brittle against a shared dev DB; unique-data injection keeps every run deterministic.

## ADR-009 — Monorepo layout (`frontend/` inside the backend repo)

- **Context**: The SPA was added after the backend was already a standalone repo-project.
- **Decision**: Keep one repository with `frontend/` beside the Maven project; Playwright
  config auto-starts both servers.
- **Alternative / why not**: separate repo + workspace — more moving parts for a single-developer
  demo; this layout keeps one `git push` shipping backend + frontend + docs together.
