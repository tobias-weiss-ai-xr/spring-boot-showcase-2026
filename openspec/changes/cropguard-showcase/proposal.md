# Proposal: CropGuard — Hail Insurance Showcase

## Why

A Spring Boot demo project that maps 1:1 to the 10-module course at
courses.graphwiz.ai/springboot. Each course module must be demonstrable
in the codebase. The domain is German agricultural hail insurance:
farmers insure plots against hail damage, file claims after hailstorms,
and assessors review and approve payouts.

## What Changes

Build the full application from the existing partial scaffold:

1. **Services**: PolicyService, ClaimService, HailEventService (domain logic, business rules)
2. **Controllers**: ClaimController, HailEventController (REST endpoints, @PreAuthorize)
3. **Security**: JWT auth (JwtService, JwtAuthFilter, SecurityConfig, AppProperties)
4. **Actuator**: DatabaseHealthIndicator (custom health check)
5. **DataInitializer**: Seed sample data (farmer, assessor, plot, policy, claim, hail event)
6. **Configuration**: application.yml, application-dev.yml (H2, JPA, actuator, JWT)
7. **Tests**: Unit (Mockito), slice (@WebMvcTest, @DataJpaTest), integration (@SpringBootTest)
8. **README**: Quick start, API tour, course-module mapping, test pyramid

## Non-Goals

- No real database (H2 in-memory only)
- No real JWT secret (hardcoded dev secret in application.yml)
- No production deployment (demo only)
- No Angular frontend (backend-only demo; the course covers REST APIs)
