# Proposal: Sapiens Alignment — CropGuard als Architektur-Spiegel von Sapiens IDITSuite

## Why

Die Vereinigte Hagelversicherung VVaG (Zielarbeitgeber, Bewerbungsgespräch am 14.10.2026) ersetzt ihre
Bestandsführung durch **Sapiens IDITSuite** (PolicyMaster/BillingMaster/ClaimsMaster, Low-Code-"Smart
Packs", API-Layer "ACE", Country-Layer, DataSuite mit Power-BI-Reports, DigitalSuite-Portale). Die
ausgeschriebene Rolle "(Senior) Software Developer" dreht sich genau um diese Modernisierung.

**Wichtig zur Frage "kommt man an den Source Code?"**: Nein. Sapiens IDITSuite ist proprietäre
kommerzielle Software (SaaS, kein öffentlicher Quellcode). Auch als Kunde/Entwickler bei der VH bekommt
man den Kern-Source nicht — man **konfiguriert** (Low-Code/"Smart Packs"), **integriert** über die
offene API-Schicht (ACE: REST/OpenAPI) und erweitert über den **Country/Customer-Layer**.
CropGuard kann Sapiens daher nicht am Code nachbauen — sondern am **Architektur-Muster**:
konfigurierte Produkte statt hartkodierter Tarife, klare Policy/Billing/Claims-Modulgrenzen,
API-als-Vertrag, Datenmigration aus einer "Legacy"-Quelle. Genau das macht dieses Change-Schiff.

## What Changes

1. **Rating-Engine als Konfiguration ("Smart Packs"-Muster)** — die hartkodierten
   Tarifdaten in den Enums (`CropType`-Basissätze, `Bundesland`-Risikofaktoren,
   `Deductible`-Faktoren) werden in konfigurierbare **Rating Packs** (YAML) ausgelagert;
   neues Produkt/Tarif = Konfigurationseintrag, kein Code, kein Redeploy. Inkl. Konzept
   "Country/Customer-Layer" (Pack-Overrides je Land, z. B. DE/PL).
2. **Modulare Master-Struktur** — Package-Grenzen `policy` / `billing` / `claims` im
   Sinne von PolicyMaster/BillingMaster/ClaimsMaster, mit Import-/Grenz-Test (keine
   Querzugriffe zwischen Modulen) — Schichtenlogik (Controller→Service→Repository)
   bleibt für die Kursmodul-Zuordnung erhalten.
3. **OpenAPI als Vertrag ("ACE"-Muster)** — OpenAPI-Beschreibung wird veröffentlicht
   (springdoc, versionierte Info), Vertrags-Smoke-Test sichert API-Stabilität;
   Frontend/Integrationen konsumieren den Vertrag.
4. **Legacy-Datenimport ("Migration")** — konfigurationsgesteuerter Import eines
   "Legacy"-CSV-Fixtures (Bestandsdaten: Kulturarten, Faktoren, Policen) mit Validierung
   und Fehlerreport — Spiegel der realen Datenmigration Altbestand → Sapiens.
5. **Dokumentation "Sapiens Mapping"** — README-Abschnitt, der jedes CropGuard-Element
   auf das Sapiens-Pendant mappt (fürs Interview).

## Non-Goals

- Kein Sapiens-Source-Code, kein Klon der Sapiens-Funktionalität
- Kein echter Multi-Tenant-/Multi-Country-Betrieb (nur Konzeptbeleg via Pack-Overrides)
- Kein zusätzlicher BPMN-Workflow-Engine-Baustein (Camunda) — Statusmaschine bleibt
  schlank im Code
- Kein Produktiv-Deployment, keine neue Datenbank (H2 bleibt)
- Keine neuen Rollen (kein ADMIN); Legacy-Import läuft über Konfigurations-Flag statt
  neuer Admin-API
