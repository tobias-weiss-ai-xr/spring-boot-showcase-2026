# Proposal: Portal-Ökosystem — CropGuard-UI als Spiegel von Mitgliederportal + Sapiens DigitalSuite

## Why

Die CropGuard-UI ist funktional, aber simplistisch: zwei flache Dashboards mit einfachen
Formularen und Listen. Das reale Ziel-Ökosystem — das Kundenportal eines führenden europäischen
Agrarversicherers (Verträge, Anbaudeklaration mit Feldstücken, Online-Schadenmeldung, Feldstück-Wetter)
und **Sapiens DigitalSuite** (Persona-Portale CustomerConnect/AgentConnect, Journey & Forms
Composer für Guided Flows, 360°-Customer-View) — zeigt, wie ein moderner
Pflanzenversicherer seine Kunden und Sachbearbeiter tatsächlich digital bedient. Ein
Bewerber, der diese Form im Demo-Nachbau lebt, zeigt im Interview, dass er das
Produkt-Ökosystem verstanden hat — nicht nur die API.

## What Changes

1. **Persona-Portale (DigitalSuite-Muster)** — FARMER bekommt ein Kundenportal
   („Übersicht / Anbau / Verträge / Schäden / Lage"), ASSESSOR eine Sachbearbeiter-Workbench
   („Aufgaben / Lagebild") — getrennte Navigationen, Rollen-Badge im Header, Deep-Links
   je Sektion (child routes statt einer einzigen Dashboard-Seite).
2. **Guided Schadenmeldung (Journey & Forms Composer-Muster, FNOL-Flow)** — die
   Schadenform wird ein mehrstufiger Wizard: Feldstück → Schadendatum/Grund →
   Ausmaß → Zusammenfassung → Absenden. Gleiche API-Payload wie heute
   (`POST /api/claims`), nur die UI-Journey ändert sich.
3. **360°-Customer-View (CustomerConnect-Muster)** — Übersichtsseite mit KPI-Karten
   (aktive Policen, Versicherungssumme, offene Schäden), Policentabelle mit
   Status-Badges und Prämie (BillingMaster-Sicht), Schaden-Timeline und
   Anbau-Verzeichnis als verwaltete Tabelle (Managed-Register-Muster: Feldstücke mit
   Kultur, Fläche, Georef-Anzeige).
4. **Sachbearbeiter-Workbench (AgentConnect-Muster)** — filterbare/sortierbare
   Schaden-Queue mit Status-Chips, Eingangs-Alter und Schwere, Schaden-Detail
   mit Assess-Panel, plus „Lagebild"-Sektion (Risikokarte + Hagelereignis-Feed).
5. **Design-System-Basis** — CSS-Design-Tokens (Palette, Spacing, Typografie,
   Radius/Schatten), konsistente Karten/Tabellen/Badges/Skeleton-Loader,
   professionelle Versicherungs-Anmutung (kein Hobby-Look), responsive und
   a11y-geprüft. Weiterhin **plain CSS, kein UI-Framework**.

## Non-Goals

- Keine Backend-Änderungen: alle Sektionen konsumieren die bestehenden Endpunkte
  (keine neuen APIs, keine Entity-Änderungen)
- Kein echtes Feldstück-Polygon-Editing / Karten-Library (Georef bleibt Anzeige der
  gespeicherten GK3-Koordinaten + Bestands-Risikokarte; keine Leaflet/OpenLayers-Abhängigkeit)
- Keine echten Wetterdaten (Feldstück-Wetter wird nicht nachgebaut; „Lage" bleibt
  DWD-Trockenindex + Hagelereignis-Feed aus Bestand)
- Kein PDF-/Dokumentenmanagement (Korrespondenz bleibt außen vor)
- Keine neue Rolle, kein Login-Flow-Umbau (Register/Login bleiben, optisch nur poliert)
