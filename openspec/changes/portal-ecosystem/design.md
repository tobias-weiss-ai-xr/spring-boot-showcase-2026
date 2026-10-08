# Design

## Context

Angular-20-Standalone-SPA mit Signals, plain CSS (kein UI-Framework), deutsche UI.
Heute: App-Shell mit Topbar + zwei Dashboard-Routen (`/farmer`, `/assessor`), die
Formulare und Listen auf einer Seite stapeln. 12 Playwright-e2e-Specs sichern die
Flows über Labels/Aria-Attribute (`exact: true`-Konvention). Zielbild: Mitgliederportal +
Sapiens-DigitalSuite-Muster (Persona-Portale, Guided Journeys, 360°-View,
Workbench), siehe proposal.md.

## Goals / Non-Goals

**Goals:** Portal-Struktur mit Deep-Links, Guided-Claim-Journey, KPI-360°-Sicht,
Workbench-Queue, Design-Tokens, professionelle Anmutung — alles ohne Backend-Änderung
und ohne die e2e-Suite zu verlieren.

**Non-Goals:** siehe proposal.md (keine Karten-Library, keine echten Wetterdaten,
kein Dokumenten-Management, kein Backend).

## Decisions

- **D1 — Eigenes Mini-Design-System auf CSS-Tokens (kein UI-Framework).**
  `styles.css` wird zur Token-Quelle (`--color-*`, `--space-*`, `--radius-*`,
  `--shadow-*`, Typografie-Skala) plus Komponenten-Klassen (`.card`, `.kpi`,
  `.table`, `.badge--*`, `.skeleton`, `.wizard__steps`). Palette: professionelles
  Versicherungs-Blau/Neutral mit Agrar-Grün als Akzent (nicht das bisherige
  Through-Green). *Alternativen:* Angular Material / Tailwind — verletzen die
  Projekt-Vorgabe „plain CSS, kein Framework"; ein unaufgeräumtes Ad-hoc-CSS
  ist der Status quo, der abgelöst werden soll. Tokens sind der Upgradepfad
  („ponytail: tokens-in-one-file; extract theme when a second app needs it").
- **D2 — Portal-Shell mit rollengetriebener Child-Navigation.** `app.html` rendert
  je `auth.user().role` ein Navigationsmenü (farmer: Übersicht/Anbau/Verträge/
  Schäden/Lage; assessor: Aufgaben/Lagebild). Neue child routes unter `/farmer/*`
  und `/assessor/*` mit `farmarGuard`/`assessorGuard` je Sektion; `/farmer` und
  `/assessor` redirecten auf die erste Sektion (alte Deep-Links bleiben gültig —
  wichtig für die e2e-Navigation und Lesezeichen). *Alternative:* Tab-Signals
  ohne Routen — keine Deep-Links, verworfen.
- **D3 — Guided Journey als eine Wizard-Komponente mit Signal-Step-State.**
  `ClaimWizard` (standalone) hält `step = signal(1..4)`, validiert je Schritt,
  Zusammenfassung vor dem Submit, baut exakt die heutige `ClaimDto`-Payload
  (`policyId, eventDate, description, hailEventId?`) — der Assessor-Flow und die
  e2e-API-Payloads bleiben unverändert. *Alternative:* Router-per-Step — mehr
  Artefakte, kein Mehrwert für 4 Schritte.
- **D4 — Geteilte UI-Primitives als standalone-Komponenten** unter
  `shared/ui/`: `Badge` (Status→Farbe zentral), `KpiCard`, `Skeleton`,
  `EmptyState`. Wiederverwendbar in beiden Portalen; verhindert per-Page-Duplikate.
- **D5 — e2e-Kompatibilität als hartes Design-Kriterium.** Alle bestehenden
  Labels/Aria-Attribute/Rollen bleiben erhalten; nur wo die Journey sichtbar
  anders ist (Schadenmeldung-Wizard), darf der e2e-Flow angepasst werden —
  und erhält einen eigenen Spec (`farmer.spec.ts`-Ergänzung oder neuer
  `claim-wizard.spec.ts`). Akzeptanz jedes UI-Tasks: `ng build` + Karma + volle
  Playwright-Suite grün. arc42-Testzählung wird im Final-Task aktualisiert.

## Risks / Trade-offs

- [Route-Umbau kann e2e-Selektoren brechen] → D5: Sektion-Routen leiten von den
  bestehenden Pfaden her (`/farmer` bleibt Einstieg); e2e läuft als Gate in jedem
  UI-Task, nicht erst am Ende.
- [Wizard fügt Klicks zwischen Login und Schaden-Submit] → Fortschritts-Indikator
  + „Zusammenfassung" halten den Flow nachvollziehbar; e2e-Neuspec deckt den
  Happy Path ab.
- [Design-Tokens in einer Datei] → bewusst; Extraktion in ein Theme ist
  dokumentierter Upgrade-Pfad.
