# 06 — Runtime View

The most important runtime scenarios, with sequence diagrams.

## Scenario 1 — Login and role-based entry

```mermaid
sequenceDiagram
  autonumber
  actor U as User
  participant SPA as Angular SPA
  participant API as Spring Boot API
  participant DB as H2

  U->>SPA: enter credentials
  SPA->>API: POST /api/auth/login {email, password}
  API->>DB: find insured by email
  API->>API: BCrypt.matches(password, hash)
  API-->>SPA: 200 {token, id, name, role}
  SPA->>SPA: store session in localStorage (AuthService)
  SPA->>SPA: AuthGuard reads role
  alt role = FARMER
    SPA-->>U: /farmer dashboard
  else role = ASSESSOR
    SPA-->>U: /assessor dashboard
  end
  Note over SPA: subsequent requests carry "Authorization: Bearer <token>"
```

Wrong credentials → `UnauthorizedException` → 401 `ProblemDetail` → shown in an alert box;
wrong role on a guarded route → "Zugriff verweigert" (forbidden) page.

## Scenario 2 — The full claim lifecycle (the demo's core journey)

```mermaid
sequenceDiagram
  autonumber
  actor F as Farmer
  participant SPA as Angular SPA
  participant API as Spring Boot API
  actor A as Assessor

  F->>SPA: adjust quote (crop, ha, Bundesland, deductible, coords)
  SPA->>API: GET /api/policies/quote
  API->>API: PremiumCalculator: base × risk × drought × deductible
  API-->>SPA: PremiumResult (premium + breakdown)
  F->>SPA: create plot
  SPA->>API: POST /api/plots (ins. id, crop, ha, Bundesland, GK3)
  API-->>SPA: 201 Plot
  F->>SPA: buy policy for plot
  SPA->>API: POST /api/policies (coverage, deductible, dates)
  API->>API: PolicyService.validate (coverage rules, premium)
  API-->>SPA: 201 Policy ACTIVE
  F->>SPA: report hail damage
  SPA->>API: POST /api/claims (damage date, description, policy)
  API->>API: ClaimService.submit — coverage window + status checks
  API-->>SPA: 201 Claim SUBMITTED
  A->>SPA: open claim queue (GET /api/claims — all)
  A->>SPA: set damage %, decision, notes
  SPA->>API: PUT /api/claims/{id}/assess
  API->>API: @PreAuthorize('ASSESSOR') → ClaimService.assess: payout = f(coverage, %, deductible)
  API-->>SPA: 200 Claim APPROVED (damagePercent, payoutEur, assessedBy)
```

## C4 Level 4 — Code diagram (example: premium calculation)

Level 4 shows a representative slice of the code — the pricing core, since it is the most
domain-critical logic. (Full class-level documentation is unnecessary for any other path.)

```mermaid
classDiagram
  class CropType {
    <<enumeration>>
    baseRateEurPerHa
    riskCategory
  }
  class Bundesland {
    <<enumeration>>
    label
    riskFactor
  }
  class Deductible {
    <<enumeration>>
    percent
    premiumFactor
  }
  class DwdRiskGridService {
    +int getDroughtIndex(e, n)
    +double getDroughtAdjustment(e, n)
  }
  class PremiumCalculator {
    +PremiumResult calculate(crop, ha, bundesland, deductible, coverage, e, n)
    -validate(hectares, coverage)
  }
  class PremiumResult {
    <<record>>
    premiumEur, baseEur, riskAdjustment, deductibleAdjustment, coverageEur
    droughtIndex, droughtAdjustment
  }

  PremiumCalculator --> DwdRiskGridService : drought lookup
  PremiumCalculator --> PremiumResult : returns
  PremiumCalculator ..> CropType : baseRateEurPerHa
  PremiumCalculator ..> Bundesland : riskFactor
  PremiumCalculator ..> Deductible : premiumFactor
```

Business rules enforced in `calculate` / `validate`:

1. `base = cropType.baseRateEurPerHa × hectares`
2. `premium = base × bundesland.riskFactor × droughtAdjustment × deductible.premiumFactor`
   (rounded to cents, `MIN_PREMIUM_EUR = 50`)
3. Coverage ≤ `MAX_COVERAGE_EUR = 500_000`
4. `coverage ≥ 10 × premium` (insurance principle)

`DwdRiskGridService` resolves a GK3 coordinate to the 1 km grid cell and returns index 0–10
(adjustment 0.8–1.5). With null coordinates the drought adjustment is skipped (1.0).
