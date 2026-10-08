# Design

## Context

CropGuard (Java 21, Spring Boot 3.3.5, JPA+H2, Security/JWT, Actuator, Angular-Frontend)
soll ein lauffähiges Architektur-Spiegelbild der Bestandsführungs-Modernisierung bei der
Vereinigten Hagelversicherung werden. Referenz-Referenz ist Sapiens IDITSuite (vgl. SAPIENS.md
im Check-Repo): proprietäres SaaS — Zugang für den Kunden = **Konfiguration (Smart Packs),
API (ACE), Country-Layer, Erweiterung**, nicht Source-Code. CropGuard bildet diese Muster
mit Open-Source-Mitteln ab.

## Goals / Non-Goals

**Goals:**
- Tarif-/Produktdaten aus dem Code in Konfiguration verschieben (Business-Änderung ohne Redeploy)
- Klare Modulgrenzen analog PolicyMaster/BillingMaster/ClaimsMaster, testbar abgesichert
- API-als-Vertrag: OpenAPI veröffentlicht und getestet
- "Legacy"-Datenimport als Migrationsbeispiel (validiert, fehlerberichtet)
- Interview-taugliche Sapiens-Zuordnung in der Doku

**Non-Goals:** siehe proposal.md (kein Sapiens-Klon, keine Camunda, kein neues DB-/Rollenmodell).

## Decisions

- **D1 — Rating Packs als YAML-Konfiguration ("Smart Packs" + Country-Layer).**
  Neue Pakete `src/main/resources/rating/` mit z. B. `default.yml` und `pl.yml`
  (Kulturarten → Basissatz/Risikokategorie, Bundesland → Risikofaktor, Selbstbehalt →
  Faktor, Mindest-/Maximal-Deckung). `RatingPackService` lädt die Packs beim Start in
  den Speicher, liefert Lookups (`baseRateFor(crop, country=DE)`), validiert beim Laden
  (unbekannte Schlüssel → fail-fast). `PremiumCalculator` löst Werte über den Service
  statt über Enum-Konstanten; die Enums bleiben als **identifizierende Schlüssel**
  (Katalog), verlieren aber die Zahlen. Overrides (z. B. Polen) = Country-Layer-Muster.
  *Alternativen:* DB-Tabellen (realistischer, aber schwergewichtiger als H2-Demo-Setup),
  reine Enum-Werte (Status quo, genau das was Sapiens ablöst). YAML gewinnt: offline,
  deterministisch, minimale Deltagröße, demonstriert das Konfig-Muster.
- **D2 — Modul-Packages `policy`/`billing`/`claims` mit Integritäts-Test.**
  Einführung eines `modules`-Pakets: `com.example.cropguard.modules.{policy,billing,claims}`.
  Jedes Modul enthält seine Service-/Repository-/DTO-Logik; Controller bleiben im
  bestehenden `controller`-Layer und delegieren in die Module (Schichtenabbildung der
  Kursmodule bleibt erkennbar). Ein einfacher **Import-Grenz-Test** (Reflection über
  package-private Klassen + `Accept-Lists`);
  erzwingt: Module greifen nicht auf Repository-/Entity-Interna *anderer* Module zu
  (lesender Zugriff nur über explizite Modul-Facades, z. B. `PolicyQueryPort`).
  *Alternative:* vollständige Maven-Multi-Module — für eine Demo überdimensioniert;
  Package-Grenzen + Test reichen und halten den Diff klein.
- **D3 — OpenAPI als Vertrag ("ACE"-Muster).** Bestehendes `OpenApiConfig` (springdoc)
  bleibt; zusätzlich: `info.version` aus Build-Version, `tags` je Modul, und ein
  **Vertrags-Smoke-Test** (Spring-MockMvc ruft `/v3/api-docs`, prüft `openapi: 3.x`
  und erwartete Pfade `/api/policies`, `/api/claims`, `/api/plots`). Damit ist der
  API-Vertrag Teil des Tests — der Kern dessen, was Sapiens mit ACE vorlebt.
- **D4 — Legacy-Import als konfigurationsgesteuerter Runner.** CSV-Fixture
  `src/main/resources/legacy/ratings_legacy.csv` (Spalten: crop, base_rate, risk,
  bundesland_factor, deductible_percent, deductible_factor) simuliert Alt-Bestandsdaten.
  `LegacyImportService` parst, validiert (unbekannte/leere/negative Werte → Fehlerzeile
  mit Grund), mappt in Pack-Daten/Muster-Policen und liefert einen Fehlerreport.
  Aktivierung über `cropguard.legacy-import.enabled` (default: false) als
  `CommandLineRunner` — **keine neue Rolle/kein neuer Endpunkt**, das Security-Modell
  bleibt FARMER/ASSESSOR. Test deckt happy path + Fehlerreport ab.
- **D5 — Doku "Sapiens Mapping".** README-Abschnitt mit Tabelle:
  PolicyMaster≈`modules.policy`/PolicyService, BillingMaster≈`modules.billing`
  (Prämie/Quote), ClaimsMaster≈`modules.claims`/ClaimService, Smart Packs≈Rating-Packs-YAML,
  Country-Layer≈Pack-Overrides, ACE≈OpenAPI+Versionierung, DigitalSuite≈Angular-Frontend,
  DataSuite≈Actuator-/Reporting-Daten (kein DWH-Umbau in diesem Change).

## Risks / Trade-offs

- [YAML statt DB-Persistence für Tarife] → Absicht: Demo-Konfigographie; Umstieg auf
  DB-Tabellen ist der dokumentierte Upgrade-Pfad ("ponytail: config-in-file, move to DB
  when a real admin UI needs it").
- [Package-Module statt Maven-Module] → Grenztest kommuniziert die Regeln; echter
  Build-Split schiebbar, wenn Module wachsen.
- [Enum-Werte bleiben als Schlüssel erhalten] → Migration der Konstanten muss Quotes
  und Katalog(-API) konsistent halten; Test sichert `seed`-Daten setzen weiter auf.
- [Legacy-CSV statisch] → bewusst, damit der Demo-Import deterministisch und offline bleibt.
