# 01 — Introduction and Goals

## Requirements Overview

CropGuard is a **demo application** for the [Spring Boot course](https://courses.graphwiz.ai/springboot)
at CertPrep. Its purpose is educational: **every course module (1–10) must be demonstrable
1:1 in the codebase**. Beyond that it is a complete, non-trivial business application in the
German agricultural hail-insurance domain.

**The domain in one sentence:** farmers insure plots (`Felder`) against hail damage; after a
hailstorm they file claims (`Schäden`); assessors (`Gutachter`) review the claims, calculate
payouts, and approve or reject them.

### Functional scope

- **Quoting & pricing** — premium calculation with two location-based risk adjustments:
  Bundesland hail-risk factor (DWD-based) and a 1 km DWD drought index grid (GK3 coordinates).
- **Policy management** — farm plots, insurance policies, coverage/deductible options.
- **Claims workflow** — `SUBMITTED → APPROVED / REJECTED` via assessor review, with payout
  calculation (further statuses `UNDER_REVIEW`, `ASSESSED`, `PAID` exist in the enum but are not
  set by any API path).
- **Hail events** — registered storms that can be linked to claims.
- **Security** — JWT authentication with two roles: `FARMER` (files claims) and
  `ASSESSOR` (assesses claims).
- **Frontend** — an Angular SPA with separate farmer and assessor views.
- **Monitoring** — Spring Boot Actuator with a custom database health indicator.

### Overflow / non-goals

- No real database (H2 in-memory only).
- No production deployment, no real JWT secret management.
- No external runtime dependencies (no live DWD API, no runtime geocoding).
- No multi-tenancy, no payments, no document upload.

## Quality Goals

| Priority | Quality | Explanation |
|----------|---------|-------------|
| 1 | **Demonstrability** | Each course module (Spring Core, REST, Validation, JPA, Security, …) must be findable in the code — structure follows the curriculum. |
| 1 | **Zero-setup demo** | `mvn spring-boot:run` boots a fully seeded, working system with sample data. |
| 1 | **Testability / correctness** | A real test pyramid: unit + slice + integration backend tests, plus E2E browser tests that prove the workflows end to end. |
| 2 | **Simplicity** | No build-fat: plain CSS (no UI framework), records for DTOs, constructor injection only, RFC 7807 problem responses. |
| 2 | **Domain fidelity** | German insurance semantics modeled accurately enough to be believable (premium formula, coverage rules, self-retention/deductible). |

## Stakeholders

| Role | Expectations |
|------|--------------|
| Course learners | Readable, well-structured Spring code that maps to the curriculum; working demo flows. |
| Course instructor | A project where every module can be pointed at and explained. |
| Reviewer (this documentation) | Architecture expressed in C4 and arc42 so the design decisions are explicit and defensible. |
