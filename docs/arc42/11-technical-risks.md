# 11 — Technical Risks

| # | Risk | Likelihood | Impact | Mitigation |
|---|------|------------|--------|------------|
| 1 | **Hardcoded JWT dev secret** — if reused in any real system, tokens are forgeable. | Low (demo-only) | High | Documented non-goal (ADR-002); secret is obviously a placeholder (`cropguard-dev-secret-…`); no production deployment exists. |
| 2 | **In-memory H2 + `create-drop` loses all data on restart.** | Certain by design | Medium | Accepted demo trade-off; `DataInitializer` reseeds identical sample data on every boot. |
| 3 | **Static DWD snapshot drifts** from the live 1991–2020 baseline (frozen dataset). | Medium (values are historical by nature) | Low | The snapshot *is* a fixed historical baseline, so it cannot "drift" factually; reproducibility was the goal. A live DWD source can replace it behind `DwdRiskGridService`. |
| 4 | **EAGER `@ManyToOne` → N+1 selects** as claim/policy data grows. | Medium | Medium | Correct only at demo scale (ADR-005). Upgrade path: response-DTO projection or fetch joins, tracked in the code comment. |
| 5 | **Entity-as-API-contract** — response shape is coupled to JPA mapping; a schema change ripples into the SPA. | Medium | Medium | ADR-005 trade-off; the frontend `models.ts` mirrors entities. Long-term fix is a proper DTO layer. |
| 6 | **Shared dev DB + parallel tests** — E2E and manual use both mutate H2. | Medium | Low | E2E seeds unique data (unique emails/descriptions, single worker), asserts on its own rows only. |
| 7 | **Machine-specific paths** in tooling (`JAVA_HOME=jdk-22`, default Maven path in `playwright.config.ts`). | High on this machine | Low | CI overrides via platform default / `MVN_CMD` (see 07); `reuseExistingServer` tolerates already-running dev servers. Follow-up: `.mvn` wrapper. |
| 10 | **Public OpenAPI/Swagger** discloses the API surface (endpoint paths, entity schemas) to unauthenticated visitors. | Certain (deliberate) | Low | Demo convenience; no data exposed, only structure. Production: gate `/swagger-ui/**` + `/v3/api-docs/**` behind auth or an allowlist. |
| 8 | **Angular CLI vs Node version coupling** — the newest CLI line wants a newer Node than the local 22.14, so the project pins Angular 20. | Medium | Low | Pinned in `package.json` (`@angular/*` 20.x); upgrade Angular and Node together. |
| 9 | **No production deployment / ops story** — health is only ever checked manually. | High (by scope) | Low | Accepted non-goal; Actuator + health indicator make it trivially containerizable later. |

## Risk register summary

Openly accepted demo risks: 2, 9, 10. Actively mitigated: 1, 3, 6. Requires follow-up if scope
ever extends beyond a demo: 4, 5, 7.
