# Implementation Tasks

## Task UI-1: Design tokens + portal shell + role navigation
**Scope:** `frontend/src/styles.css`, `frontend/src/app/app.{html,ts,css}`, `frontend/src/app/app.routes.ts`, `frontend/src/app/pages/login/*`, `frontend/src/app/pages/register/*`, `frontend/src/app/core/auth.guard.ts`, `frontend/src/e2e/**`
**Accept:** `cd frontend && npx ng build && CI=true npx playwright test`
Introduce the token design system (palette/spacing/typography/radius/shadows + shared card/table/badge/skeleton styles) in styles.css; restructure the app shell to render role-specific portal navigation (farmer: Übersicht/Anbau/Verträge/Schäden/Lage; assessor: Aufgaben/Lagebild) with role badge; split the two dashboards into child section routes under /farmer/* and /assessor/* with per-section guards and redirects from the legacy entry routes; polish login/register with the new look (split panel, demo-credentials hint kept). Preserve every existing label/aria attribute used by the e2e suite; adjust e2e navigation only where routes changed and keep the suite green.

## Task UI-2: Farmer customer-360 (Übersicht + Anbau + Verträge)
**Scope:** `frontend/src/app/pages/farmer/**`, `frontend/src/app/shared/ui/**` (new), `frontend/src/styles.css`
**Accept:** `cd frontend && npx ng build && CI=true npx playwright test`
Build shared UI primitives (Badge, KpiCard, Skeleton, EmptyState) as standalone components; create the farmer Übersicht section with KPI cards (active policies, total coverage, open claims), policy table with status badges + premium column and a recent-claims timeline; rework Anbau into a managed plot table (crop, area, Bundesland, GK3 coordinates display) with the create form in a side panel (WEB AV pattern); show Verträge as the policy detail table. All data from existing endpoints with skeleton loading; German labels throughout.

## Task UI-3: Guided claim journey (FNOL wizard)
**Scope:** `frontend/src/app/pages/farmer/**` (claim wizard), `frontend/src/app/shared/ui/**`, `frontend/src/e2e/**`
**Accept:** `cd frontend && npx ng build && CI=true npx playwright test`
Replace the flat claim form with a multi-step ClaimWizard (step 1 field/policy, step 2 event date + cause/hail event, step 3 extent/description, step 4 summary + confirm) with step indicator, per-step validation and step-back; submit builds the identical POST /api/claims payload; success keeps the existing notify() feedback and lands in the Schäden section. Keep existing e2e claim flow passing — add one wizard happy-path scenario if the flow visibly changed. List of claims gets status badges and event linkage display.

## Task UI-4: Assessor workbench + Lagebild
**Scope:** `frontend/src/app/pages/assessor/**`, `frontend/src/app/risk-map/**`, `frontend/src/app/shared/ui/**`, `frontend/src/styles.css`
**Accept:** `cd frontend && npx ng build && CI=true npx playwright test`
Rework the assessor portal into a workbench: Aufgaben section with a filterable/sortable claim queue (status chips, entry age, severity), claim detail view with the assess panel (decision select + assessment text) and customer/policy context; Lagebild section combining the DWD risk map (role=img, existing aria-label preserved) and the hail-event feed with severity badges; workbench KPI row (open items, total claimed amount). Existing assess e2e flow must pass unchanged.

## Task UI-5: Polish sweep + full regression + docs
**Scope:** `frontend/src/**`, `README.md`, `docs/arc42/05-building-block-view.md`, `docs/arc42/10-quality-requirements.md`, `pom.xml`, `.github/workflows/ci.yml`
**Accept:** `JAVA_HOME="/c/Program Files/Java/jdk-22" mvn -q verify && (cd frontend && npx ng build && CI=true npx playwright test)`
Apply the design system consistently across every remaining page (register, forbidden, error states), add responsive behavior (usable at 360px width: tables scroll or collapse to cards), a11y pass (AA contrast on badges/buttons, focus-visible everywhere, form labels intact), skeleton/empty states in every list, and micro-transitions (hover/focus only, no motion excess). Then full regression: backend mvn verify untouched-green, ng build, Karma, full Playwright suite. Update README (persona portals, claim journey) and arc42 05 (portal sections in building block view) + 10 (current test totals) — counts must match the verified suites exactly.
