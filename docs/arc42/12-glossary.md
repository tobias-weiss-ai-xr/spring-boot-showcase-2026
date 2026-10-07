# 12 — Glossary

| Term | Definition |
|------|------------|
| **CropGuard** | The demonstrated system: German agricultural hail-insurance management. |
| **Farmer (Landwirt)** | User role owning plots, policies and claims (seeded: `max@bauernhof.de`). |
| **Assessor (Sachverständiger)** | User role reviewing and settling claims (seeded: `lisa@cropguard.de`). |
| **Plot (Feld)** | An insured field: crop type, hectares, Bundesland, GK3 coordinates, description. |
| **Policy (Police)** | Insurance contract: coverage, premium, deductible, status (`ACTIVE`…), coverage window, bound to a plot. |
| **Claim (Schaden)** | Damage report: date, description, `SUBMITTED → ASSESSED → APPROVED/REJECTED`, damage %, payout. |
| **Hail event (Hagelereignis)** | Registered storm: date, affected Bundesländer, severity, hailstone diameter; claims can be linked to it. |
| **Premium (Prämie)** | Annual price of a policy: `base × riskFactor × droughtAdjustment × deductibleFactor`. |
| **Deductible (Selbstbehalt)** | `NONE`, `FIVE/TEN/FIFTEEN/TWENTY_PERCENT`; lowers premium via `premiumFactor` (1.0 → 0.72). |
| **Coverage (Deckung)** | Max guaranteed payout, capped at €500,000 and ≥ 10× premium. |
| **Bundesland** | German federal state; each carries a DWD-based hail risk factor (Bayern 1.5 … Schleswig-Holstein 0.8) and a display label. |
| **CropType** | Planted crop with a base rate €/ha (WHEAT €45 … HOPS €350) and a risk category. |
| **DWD drought index** | 1 km raster (July 1991–2020, CC-BY 4.0) giving a 0–10 index; maps to a premium-adjustment factor 0.8–1.5. |
| **GK3 coordinates** | Gauss-Krüger zone 3 easting/northing (meters) used to look up the drought grid cell. |
| **Open-Session-In-View (OSIV)** | Spring feature keeping the Hibernate session open through view rendering; disabled here, hence the EAGER associations (ADR-005). |
| **RFC 7807 `ProblemDetail`** | Standard JSON error body: `type, title, status, detail, instance`. |
| **JWT** | JSON Web Token carrying `sub` (email) and `role`; signed HS512 with the dev secret, 24 h TTL. |
| **Quote** | Live premium preview in the farmer SPA (`GET /api/policies/quote`), showing the full price breakdown. |
