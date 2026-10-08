# Spec Delta

## Purpose

Die CropGuard-SPA soll das reale Portal-Ökosystem eines führenden europäischen Agrarversicherers (dem Mitgliederportal:
Anbaudeklaration, Online-Schadenmeldung, Feldstück-Sicht) und die Sapiens-DigitalSuite-Muster
(Persona-Portale, Guided Journeys/FNOL, 360°-Customer-View, Workbench) im Nachbau spiegeln —
ausschließlich über die bestehende REST-API, mit stabiler e2e-Suite.

## ADDED Requirements

### Requirement: Persona-based portals with role-specific navigation
The SPA SHALL present two distinct persona portals driven by the authenticated role: a
customer portal for FARMER (sections: Übersicht, Anbau, Verträge, Schäden, Lage) and a
claims workbench for ASSESSOR (sections: Aufgaben, Lagebild), each with its own navigation,
role badge in the header, and deep-linkable section routes under `/farmer/*` and `/assessor/*`.
Legacy entry routes (`/farmer`, `/assessor`) SHALL redirect to the first section.

#### Scenario: Farmer sees the customer portal navigation
- **WHEN** a FARMER is authenticated and opens `/farmer`
- **THEN** the shell shows the customer-portal navigation (Übersicht/Anbau/Verträge/Schäden/Lage)
  and lands on the Übersicht section; `/farmer/verträge` is directly addressable

#### Scenario: Assessor never sees customer navigation
- **WHEN** an ASSESSOR is authenticated
- **THEN** only the workbench navigation (Aufgaben/Lagebild) is rendered, and `/farmer/*`
  is denied by the existing guard

### Requirement: Guided claim journey (FNOL wizard pattern)
Submitting a claim SHALL be a multi-step guided journey (field → event → extent → summary →
submit) with a visible step indicator, per-step validation, and a summary view before
submission. The wizard SHALL produce the same `POST /api/claims` payload as the previous form.

#### Scenario: Wizard guides through steps and submits the same payload
- **WHEN** a FARMER completes the claim wizard for an insured plot
- **THEN** the request sent to `/api/claims` is byte-equivalent in fields to the previous
  form payload, and the claim appears in the farmer's claim list with status SUBMITTED

#### Scenario: Summary step prevents accidental submission
- **WHEN** the wizard reaches the summary step
- **THEN** all entered values are displayed read-only and submission requires an explicit
  confirm action

### Requirement: Customer 360 overview (CustomerConnect pattern)
The FARMER Übersicht section SHALL aggregate the customer situation in one view: KPI cards
(active policies, total coverage, open claims), a policy table with status badges and premium
(billing visibility), and a recent-claims timeline.

#### Scenario: Overview aggregates without extra endpoints
- **WHEN** the Übersicht section loads
- **THEN** KPI cards, policy table and claim timeline are rendered exclusively from the
  existing endpoints (`/api/policies`, `/api/claims`, `/api/plots`) with skeleton loading states

### Requirement: Assessor workbench with Lagebild (AgentConnect pattern)
The ASSESSOR workbench SHALL provide a filterable claim queue (status chips, age of entry,
severity) with an assess panel per claim, and a Lagebild section combining the DWD risk map
and the hail-event feed.

#### Scenario: Queue filtering narrows the worklist
- **WHEN** the assessor filters the queue by status SUBMITTED
- **THEN** only SUBMITTED claims remain actionable in the list, and assessment still
  works end-to-end via the existing `PUT /api/claims/{id}/assess`

#### Scenario: Lagebild combines map and feed
- **WHEN** the assessor opens the Lagebild section
- **THEN** the DWD risk map canvas (existing aria-label preserved) and the hail-event
  list are shown together

### Requirement: Design system foundation with tokens
The SPA SHALL use a token-based stylesheet (`--color-*`, `--space-*`, `--radius-*`,
typography scale) with shared component styles for cards, tables, status badges, KPI cards
and skeletons; the look SHALL read as a professional insurer product (not a prototype).
Accessibility basics SHALL hold: WCAG-AA contrast for text/badges, visible focus states,
and German UI throughout.

#### Scenario: Restyling does not change behaviour
- **WHEN** the design system is applied to all portal sections
- **THEN** all 12 existing Playwright scenarios still pass (labels/aria preserved), and
  the wizard adds at most one new scenario

### Requirement: Documentation stays in sync
README and arc42 docs SHALL be updated in the same change: portal structure (05 building
block view), test counts after the UI regression (10), and a short note that the portal
mirrors dem Mitgliederportal/DigitalSuite patterns (README, Sapiens Mapping section).

#### Scenario: Docs reflect the new UI
- **WHEN** the change is complete
- **THEN** README mentions the persona portals and the claim journey, and arc42 05/10
  describe the portal sections and current test totals
