# Design

## Context

CropGuard is a demo backend for the Spring Boot course (courses.graphwiz.ai/springboot). See proposal.md for the motivation; specs/modules/spec.md captures the coverage contract. Stack: Java 21, Spring Boot 3.3.5, Spring Data JPA + H2, Spring Security + jjwt 0.12.6, Spring Validation, Actuator. Every course module must be demonstrable in the code.

## Goals / Non-Goals

**Goals:**
- A layered, convention-clean codebase that mirrors the 10 course modules 1:1
- Single-command demo (`mvn spring-boot:run`) with seeded data and JWT auth for role demo (FARMER vs ASSESSOR)
- Location-aware pricing: Bundesland hail risk factors + 1km DWD drought grid

**Non-Goals:**
- No real database or external DWD API at runtime
- No real secret management, no production hardening, no frontend

## Decisions

- **Layered package structure** (`controller` → `service` → `repository` → `entity`, plus `dto`/`exception`/`security`/`config`/`actuator`) — each directory demonstrates a course module. Alternative (feature packages) rejected: it would blur the module-to-layer mapping that is the project's whole point.
- **DTOs as Java records with Bean Validation** — immutable, zero boilerplate, idiomatic since Boot 3; avoids Lombok.
- **jwt via jjwt 0.12.6** (API/impl/jackson split) — the course's library. Dev secret lives in `application.yml`; explicitly non-production (documented in README and proposal).
- **Stateless security**: `JwtAuthFilter` (OncePerRequestFilter) hands `JwtService`-parsed authorities to the SecurityContext; `SecurityConfig` is stateless, BCrypt-based, method security with `@PreAuthorize`. Enables course Module 8 demo without sessions.
- **DWD drought grid as committed static dataset** (GK3 1km grid, 1991–2020 baseline, CC-BY 4.0) loaded into memory at startup by `DwdRiskGridService` and queried per plot coordinate for the 0.8–1.5x premium factor. Alternatives (live DWD API, GeoServer) rejected — runtime dependency would break the offline demo. The live-API path is the documented upgrade route.
- **H2 in-memory, `create-drop` + `DataInitializer` seeding** one of each entity so every endpoint works immediately after boot.
- **RFC 7807 `ProblemDetail`** via `@RestControllerAdvice` for 400/404 mapping — course Module 6.
- **One custom actuator `DatabaseHealthIndicator`** (SELECT COUNT) on top of the auto-configured health groups.

## Risks / Trade-offs

- [H2 in-memory: data lost on restart] → acceptable for demo; `DataInitializer` reseeds on every boot.
- [Static DWD snapshot drifts from live data] → fixed 1991–2020 baseline is intentional for reproducibility; swap `DwdRiskGridService` source behind its interface to go live.
- [Hardcoded dev JWT secret] → demo-only, flagged as non-goal; course Module 8 is about the mechanics, not secret hygiene.
- [Role checks trust JWT `role` claim] → fine for the demo; a real deployment requires an identity provider.
