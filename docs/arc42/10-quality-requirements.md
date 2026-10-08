# 10 — Quality Requirements

## Quality tree

| Quality aspect | Quality goal | Verification |
|----------------|--------------|--------------|
| Correctness | Claim workflow and premium math must be right | `ClaimServiceTest`, `PolicyServiceTest`, `PremiumCalculator` rules covered |
| Usability / Accessibility | Token design on every page, usable at 360 px, AA contrast + focus states, skeleton/empty states | Manual a11y checklist (contrast ratios, `:focus-visible`, labels); responsive layout check at 360 px; flows covered by the Playwright suite |
| Testability | Real pyramid at three+ levels | 42 backend tests, 2 Karma, 14 Playwright |
| Demonstrability | Every course module pointable in code | Structure conventions; `docs/arc42` |
| Performance | Fine for demo scale (in-memory, tiny data) | Not load-tested; risk noted (ADR-005 N+1) |
| Simplicity | No heavy dependencies; boring, learnable code | Dependency count minimal (12 Maven deps), no UI framework |

## Quality scenarios

### Q1 — Farmer completes the entire insurance journey (correctness + integration)
- **Context**: logged-in farmer wants to insure a plot and report a hail loss.
- **Scenario**: quote → create plot → buy policy → file claim → track status.
- **Expectation**: premium shown includes Bundesland + drought adjustments; claim appears as
  `SUBMITTED`. *Covered by* `farmer.spec.ts`.

### Q2 — Assessor reviews and settles a claim (authz + correctness)
- **Context**: assessor opens the queue.
- **Scenario**: pick a submitted claim, set damage %, decide APPROVED, enter notes.
- **Expectation**: payout appears (coverage × % adjusted by deductible); claim status flips to
  `APPROVED`; a FARMER attempting the same call receives 403. *Covered by* `assessor.spec.ts`
  and `ClaimControllerTest`.

### Q3 — Unauthenticated / unauthorized access is rejected (security)
- **Scenario**: request a protected endpoint without a token, or with a FARMER token on the
  assess endpoint; wrong credentials at login.
- **Expectation**: 401 for bad login, 403 for wrong role, redirect to `/login` on the SPA.
  *Covered by* `auth.spec.ts`, `ClaimControllerTest`.

### Q4 — Invalid input is rejected with a clear contract error
- **Scenario**: negative coverage, blank claim description, damage date outside the coverage
  window.
- **Expectation**: 400 `ProblemDetail` with field/message detail; inline alert in the UI.
  *Covered by* `PolicyControllerTest`, `ClaimControllerTest`, validation in `ClaimService`.

### Q5 — Application health is observable (operations)
- **Scenario**: `GET /actuator/health`.
- **Expectation**: `UP` with a `database` component reporting the seeded `insureds` row count.
  *Covered by* `DatabaseHealthIndicator` + `CropGuardApplicationTest` context.

## Test inventory (current, all green)

| Suite | Count | Command | What it proves |
|-------|-------|---------|----------------|
| Backend (JUnit + Mockito + slices) | 42 | `mvn test` | Context loads; premium validation via rating packs (fail-fast on invalid YAML, country override); submit/assess; controller contracts + status codes; repository queries; DWD grid lookups; ownership/IDOR rejections; assess decision whitelist; forced-FARMER registration; module boundary integrity; OpenAPI contract paths; legacy CSV import happy path + failure report |
| Frontend unit (Karma) | 2 | `npm test` | AuthService login request, role signal, logout |
| Frontend E2E (Playwright) | 14 | `npm run test:e2e` | Login/registration both roles, wrong creds, portal section nav + legacy redirect, role guards both directions, live quote recalc, full plot→policy→claim lifecycle incl. 4-step FNOL wizard (summary + step-back + hail-event linkage), assessor assess workflow with payout, hail-event feed, farmer claim scoping (IDOR), DWD risk map rendering |

> All three suites run against the same code and were executed green before these documents
> were written.
