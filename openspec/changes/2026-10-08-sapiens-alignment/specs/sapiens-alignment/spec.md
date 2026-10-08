# Spec Delta

## Purpose

CropGuard is a demo application for a German agricultural hail insurer whose Bestandsführung
is being modernised with Sapiens IDITSuite (PolicyMaster/BillingMaster/ClaimsMaster; config-driven
"Smart Packs"; open API layer "ACE"; country layer). Sapiens has no public source code — customers
configure and integrate. This change aligns CropGuard with those architectural patterns so the demo
is a live, interview-ready mirror of the VH transformation context. It builds on (does not replace)
the course-module mapping in `openspec/specs/modules/spec.md`.

## ADDED Requirements

### Requirement: Configurable rating packs (Smart Packs pattern)
Rating parameters (crop base rates & risk categories, Bundesland risk factors, deductible premium
factors) SHALL be externalised from enums into loader-validated YAML rating packs resolved at
runtime by a RatingPackService; enums remain identifying catalog keys. A per-country override pack
(country layer) SHALL allow overriding any rating value without code change.

#### Scenario: A rating value comes from config, not code
- **WHEN** `RatingPackService` is started with the default rating pack
- **THEN** `baseRateFor(CropType.WHEAT, DEFAULT)` returns the value from the YAML pack and the
  `CropType` enum constant carries no base-rate number

#### Scenario: Invalid pack fails fast
- **WHEN** a rating pack YAML contains an unknown key or a non-positive rating value
- **THEN** application startup fails with a descriptive error before serving traffic

#### Scenario: Country override wins over default
- **WHEN** a quote is rated for a country that defines an override pack
- **THEN** the override's value is used instead of the default pack value

### Requirement: Policy/Billing/Claims module boundaries
The core domain SHALL be organised into modules `policy`, `billing` and `claims` (Sapiens
PolicyMaster/BillingMaster/ClaimsMaster pattern) with controllers delegating into modules; the
layer mapping (controller → service → repository) of the course spec SHALL remain recognisable.

#### Scenario: Cross-module access is blocked
- **WHEN** the integration/architectural test inspects service and repository classes of `policy`,
  `billing` and `claims`
- **THEN** no module directly accesses the repositories/entities of another module except through
  an explicit allowance (facade/port), enforced by the test

#### Scenario: Endpoints stay stable through the refactor
- **WHEN** `POST /api/policies/quote`, `POST /api/claims` and `PUT /api/claims/{id}/assess` are
  exercised against the refactored code
- **THEN** they behave exactly as before the module refactor (same status codes, same payloads)

### Requirement: OpenAPI as published, tested contract (ACE pattern)
The application SHALL serve a versioned OpenAPI document (springdoc) describing the API as an
installable contract, and a test SHALL verify the document conforms to OpenAPI 3 and contains the
core resource paths.

#### Scenario: Contract document served
- **WHEN** `GET /v3/api-docs` is requested in a test slice
- **THEN** the response is `application/json` with `openapi: 3.x` and contains `/api/policies`,
  `/api/claims` and `/api/plots`

### Requirement: Legacy data import with validation report
A config-gated (off by default) startup import SHALL read a committed legacy CSV fixture, validate
each record, map it into rating-pack data, and report per-record validation failures; invalid rows
SHALL not abort the whole import.

#### Scenario: Import maps valid rows and reports invalid ones
- **WHEN** the legacy import runs over a fixture containing valid and invalid rows
- **THEN** valid rows are applied to the rating data, invalid rows are collected in a report with
  the reason for each, and startup continues

### Requirement: Sapiens mapping documentation
The README SHALL document a mapping table from CropGuard elements to Sapiens IDITSuite concepts
(PolicyMaster/BillingMaster/ClaimsMaster, Smart Packs, country layer, ACE API layer, DigitalSuite,
DataSuite), so the alignment is auditable in the repo.

#### Scenario: Mapping table present
- **WHEN** the README is inspected
- **THEN** it contains a "Sapiens Mapping" section referencing `PolicyMaster`, `ClaimsMaster` and
  "Smart Packs"
