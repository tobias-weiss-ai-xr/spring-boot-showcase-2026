# CropGuard — Software Architecture Documentation (arc42)

> CROP GUARD — Crop Insurance Management System
> Documentation of the software architecture of the CropGuard demo application.

This documentation follows the [arc42 template](https://arc42.org) (12 sections). The
architecture is described at four abstraction levels using the [C4 model](https://c4model.com):

| C4 Level | Diagram | Section |
|----------|---------|---------|
| **1 — System Context** | System context (external actors + systems) | [03 Context and Scope](03-context-and-scope.md) |
| **2 — Container** | Angular SPA, Spring Boot API, H2, DWD grid | [05 Building Block View](05-building-block-view.md) |
| **3 — Component** | Controllers, services, repositories, security | [05 Building Block View](05-building-block-view.md) |
| **4 — Code** | Premium calculation / claim assessment (example) | [06 Runtime View](06-runtime-view.md) |

## Document overview

| # | Section | Content |
|---|---------|---------|
| 01 | [Introduction and Goals](01-introduction-and-goals.md) | Requirements overview, quality goals, stakeholders |
| 02 | [Constraints](02-constraints.md) | Technical, organizational, and convention constraints |
| 03 | [Context and Scope](03-context-and-scope.md) | Business + technical context, C4 Level 1 |
| 04 | [Solution Strategy](04-solution-strategy.md) | The core architectural decisions at a glance |
| 05 | [Building Block View](05-building-block-view.md) | C4 Level 2 (containers) + Level 3 (components) |
| 06 | [Runtime View](06-runtime-view.md) | Login, quote, claim lifecycle — sequences + C4 Level 4 |
| 07 | [Deployment View](07-deployment-view.md) | Local dev topology, ports, proxy |
| 08 | [Cross-cutting Concepts](08-crosscutting-concepts.md) | Security, error handling, validation, config, testing |
| 09 | [Architecture Decisions](09-architecture-decisions.md) | ADRs with rationale and alternatives |
| 10 | [Quality Requirements](10-quality-requirements.md) | Test pyramid, quality scenarios |
| 11 | [Technical Risks](11-technical-risks.md) | Known risks and mitigations |
| 12 | [Glossary](12-glossary.md) | Domain and technical terms |

## Rendering the diagrams

All C4 diagrams are written in **Mermaid C4 syntax**. They render in most local Markdown
editors (VS Code with a Mermaid preview extension, Typora, Obsidian) and on
[mermaid.live](https://mermaid.live). GitHub's issue/wiki renderer does **not** render C4
diagrams yet — paste the code blocks into mermaid.live to view them there.

## Quick start

```bash
# backend (http://localhost:8080)
mvn spring-boot:run

# frontend (http://localhost:4200, proxies /api to :8080)
cd frontend && npm start

# tests
mvn test                                             # 23 backend tests
cd frontend && npm test                              # 2 Karma unit tests
cd frontend && npm run test:e2e                      # 11 Playwright end-to-end tests
```
