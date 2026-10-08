# CropGuard — Epics & User Stories (Produkt-Roadmap)

Stand: 2026-10-08 · Quelle: Ökosystemanalyse führender Agrarversicherer (README „Sapiens Mapping“, arc42 03/04).
Zweck: Jede künftige OpenSpec-Change hängt an genau einem Epic hier — kein Feature ohne Story.

## Personas

| Persona | Rolle im System | Ziel |
|---|---|---|
| **Landwirt:in** (FARMER) | Kunde/Versicherte | Feldstücke versichern, Prämie kennen, Schäden melden, Auszahlung erhalten — ohne Papierkram |
| **Sachbearbeiter:in** (ASSESSOR) | Schadenbüro | Schäden fair, schnell und nachvollziehbar bewerten und regulieren |
| **Betrieb/IT** (indirekt) | Versicherer | Produkte konfigurieren statt programmieren, Bestände migrieren, alles revisionssicher |

## Epics

### E1 — Vertrieb & Anbaudeklaration (Sapiens: PolicyMaster + Anbaudeklaration)
| # | User Story | Akzeptanz (Skizze) | Design-Implication |
|---|---|---|---|
| 1.1 ✅ | Als Landwirt kalkuliere ich die Prämie für ein Feldstück vor dem Abschluss | Quote aus Kultur/Fläche/Bundesland/Selbstbehalt/Lage | Rating-Packs (YAML) statt Enum-Zahlen |
| 1.2 ✅ | Als Landwirt verwalte ich meine Feldstücke mit Kultur, Fläche und Georef | Anbau-Tabelle + Side-Panel-Formular | Plot-CRUD, GK3-Koordinaten |
| 1.3 ✗ | Als Landwirt schließe ich online eine Police ab (Quote → Antrag → aktiv) | Wizard erzeugt Policy `ACTIVE`, Prämie fix | **Policy-Statusmaschine erweitern** (vgl. `Claim.Status`-Vorbild), Antrag = POST `/api/policies` mit Quote-Referenz |
| 1.4 ✗ | Als Landwirt deklariere ich jährlich meinen Anbau um (Kultur je Feldstück ändern) | Änderung erzeugt neue Prämie + Historieneintrag | Plot-Revisionen (auditierbar), Policy-Verlängerungslogik — kein In-Place-Überschreiben |
| 1.5 ✗ | Als Landwirt sehe ich mein Policendokument (Deckungsumfang, Bedingungen) | PDF/HTML-Dokument je Policy | Dokumenten-Generierung (statisch aus Rating-Pack + Policy), kein Speicherbedarf |

### E2 — Schadenmanagement (Sapiens: ClaimsMaster, FNOL → Regulierung)
| # | User Story | Akzeptanz | Design-Implication |
|---|---|---|---|
| 2.1 ✅ | Als Landwirt melde ich einen Schaden geführt in 4 Schritten | Wizard, Zusammenfassung, identische Payload | Journey-Komponente, API-Contract unverändert |
| 2.2 ✅ | Als Sachbearbeiter arbeite ich eine priorisierte Queue ab | Filter/Sortierung, Assess-Panel mit Kontext | Workbench, Boundary-Test gegen Moduldurchgriff |
| 2.3 ✗ | Als Landwirt lade ich Fotos/Dokumente zum Schaden hoch | Multipart-Upload, Vorschau im Schaden-Detail | **Dateiabstraktion** (lokal/`BlobStore`-Interface), `ClaimDocument`-Entity, Größen-/Typ-Limits an der Trust-Boundary |
| 2.4 ✗ | Als Beteiligter sehe ich den Schadenverlauf (wer/wann/was) | Timeline: SUBMITTED→UNDER_REVIEW→ASSESSED→PAID | **Claim-Event-Log** (append-only), Statuswechsel schreiben Event statt nur Feld — Basis für Audit (E6) |
| 2.5 ✗ | Als Sachbearbeiter dokumentiere ich die Auszahlung | Status `PAID` + Betrag/Datum | Settlement-Datensatz, Übergang nur aus `APPROVED` (Whitelist-Muster vorhanden) |

### E3 — Wetter & Prävention (Agrarversicherer-Portal: Feldstück-Wetter, Hagelwarnung)
| # | User Story | Akzeptanz | Design-Implication |
|---|---|---|---|
| 3.1 ✅ | Als Landwirt/Sachbearbeiter sehe ich das Risikolagebild (Trockenindex, Hagelereignisse) | Lage-Sektion mit Karte + Feed | DWD-Grid-Service, HailEvent-Feed |
| 3.2 ✗ | Als Landwirt sehe ich je Feldstück den Hagel-Ereignis-Bezug (betroffen ja/nein) | Feldstück-Detail listet nahe Ereignisse | Geodistanz-Lookup GK3 (Grid-Infrastruktur vorhanden), fachliche Nähe-Regel im `claims`-Modul |
| 3.3 ✗ | Als Landwirt werde ich benachrichtigt, wenn ein gemeldetes Hagelereignis mein Feldstück betrifft | In-App-Hinweis + Badge im Portal | **Notification-Modul** (Outbox-Pattern), Auslösung beim HailEvent-Import — der erste Scheduled-Job der Anwendung |
| 3.4 ✗ | Als Landwirt sehe ich den Niederschlags-/Trockenrückblick meines Landkreises | Diagramm aus DWD-Grid-Reihen | vorkompilierter Monatsindex, SVG-Chart ohne Dependency |

### E4 — Abrechnung (Sapiens: BillingMaster)
| # | User Story | Akzeptanz | Design-Implication |
|---|---|---|---|
| 4.1 ✅ | Als Landwirt sehe ich Prämie und Selbstbehalt meiner Police | Prämien-Spalte im 360°-View | `billing`-Modul rechnet (Rating-Packs) |
| 4.2 ✗ | Als Landwirt erhalte ich zur Police eine Beitragsrechnung mit Zahlungsziel | Invoice-Liste, Status OFFEN/GEZAHLT | `Invoice`-Entity im `billing`-Modul, Erzeugung bei Policy-Abschluss (1.3) — Module koppeln nur über Ports |
| 4.3 ✗ | Als Landwirt profitiert Schadenfreiheit von meiner Prämie | Rabattstufe je schadenfreiem Jahr | Regel ins Rating-Pack (Konfiguration, nicht Code) |

### E5 — Vermittler-Portal (Sapiens: AgentConnect) — bewusst zurückgestellt
| # | User Story | Design-Implication |
|---|---|---|
| 5.1 ✗ | Als Vermittler verwalte ich Kunden und schließe Policen im Namen ab | Dritte Rolle + Mandanten-Zuordnung auf `Insured`; `@PreAuthorize`-Muster vorhanden, aber Rollenmodell + Daten-Scope ist ein eigenes Change |
| 5.2 ✗ | Als Vermittler sehe ich mein Portfolio (Bestand, Prämiensumme) | Aggregation über fremde Kunden — nur mit sauberem Ownership-/Scope-Konzept (IDOR-Gefahr) |

### E6 — Plattform & Betrieb (Sapiens: ACE/DataSuite-Betriebssicht) — Interview-Währung
| # | User Story | Akzeptanz | Design-Implication |
|---|---|---|---|
| 6.1 ✅ | Als Betrieb konfiguriere ich Produkte ohne Redeploy | Rating-Packs + Country-Layer | YAML-Loader, Fail-Fast |
| 6.2 ✅ | Als Betrieb migriere ich Altbestände kontrolliert | Legacy-CSV-Import mit Fehlerreport | Flag-gesteuerter Runner |
| 6.3 ✅ | Als Integration verlasse mich auf den API-Vertrag | OpenAPI 3 getestet | Contract-Test |
| 6.4 ✗ | Als Revisor sehe ich, wer wann welchen Schaden bewertet hat | Audit-Einträge für jeden Statuswechsel/Assess | Interceptor/Event-Listener auf 2.4-Events, append-only Tabelle |
| 6.5 ✗ | Als Betrieb beobachte ich Latenzen/Fehlerraten je Endpunkt | Micrometer-Metriken, Health erweitert | Actuator + MeterRegistry, Dashboard ohne DWH-Umbau |
| 6.6 ✗ | Als Betrieb porte ich das System auf PostgreSQL | Prod-Profil weicht nur in Datasource | bereits vorbereitet (Env-Overrides, Flyway als Upgrade-Pfad) |

### E7 — Kundenkommunikation (Sapiens: DigitalSuite Engagement)
| # | User Story | Design-Implication |
|---|---|---|
| 7.1 ✗ | Als Landwirt werde ich über Statuswechsel informiert | Notification-Modul aus 3.3, Auslöser: Claim-/Policy-Events (2.4) |
| 7.2 ✗ | Als Landwirt schreibe ich dem Schadenbüro eine Rückfrage | `ClaimMessage`-Entity, Thread im Detail — erst nach 2.4 sinnvoll |

## Wie Stories in Design übersetzt werden (Prinzipien)

1. **Statusmaschinen statt Booleans** — jeder Lebenszyklus (Policy 1.3, Claim 2.5) ist eine
   Enum + Whitelist-Übergänge (Muster aus `Claim.Status` vorhanden) und schreibt ein Event.
2. **Modul-Ownership zuerst** — neue Fachlichkeit landet in `policy`/`billing`/`claims`
   (oder neu: `notification`); Boundary-Test zwingt Ports statt Durchgriff. Controller-Layer
   bleibt dünn.
3. **Konfiguration vor Code** — fachliche Zahlen (Rabattstufen 4.3, Nähe-Regeln 3.2) gehören
   in Rating-Packs; nur Prozedur gehört in Java.
4. **API-Contract geht vor UI** — jede Story definiert zuerst Endpoint+Payload (Contract-Test),
   die UI konsumiert; Wizard/Portal sind Austauschbar.
5. **Events statt Polling-Inseln** — 2.4/3.3/6.4/7.x bauen auf einem append-only Event-Log
   auf: einmal gebaut, tragen Audit, Notification und Timeline gemeinsam (einzige geplante
   übergreifende Infrastruktur).
6. **e2e bleibt Hartwährung** — pro Story mindestens ein Szenario; Labels/Aria-Attribute
   sind Vertragsbestandteil (Stand: 14 Playwright).

## Traceability

| Existierende Change | Bedient Epic |
|---|---|
| `sapiens-alignment` (Rating-Packs, Module, OpenAPI, Legacy-Import) | E6.1–6.3, E1.1 |
| `portal-ecosystem` (Persona-Portale, FNOL-Wizard, Workbench, Design-System) | E1.2, E2.1, E2.2, E3.1, E4.1 |
| Persistence-Deploy (H2-Volume) | E6.6-Vorarbeit |

## Priorisierung (nächste Changes)

1. **E2.3 + E2.4 (Fotos + Claim-Event-Log)** — höchster Demo-Wert („realer Schadenfall"),
   Event-Log ist Fundament für 6.4/7.1, Storage-Abstraktion zeigt Trust-Boundary-Handwerk.
2. **E3.2 + E3.3 (Feldstück-Ereignisbezug + Benachrichtigung)** — der Wettbewerbsvorteil
   (Wetter datengetrieben), erster Scheduled-Job + Outbox.
3. **E4.2 (Rechnungen)** — füllt BillingMaster-Story, koppelt Module über Ports.
4. **E6.4 + E6.5 (Audit + Metriken)** — Betriebssicht, billige Punkte auf dem Event-Log.

Bewusst nach dem Interview (14.10.): E1.3/1.4 (Policy-Abschluss + Deklarationsjahr),
E5 (Vermittler), E7.2 (Nachrichten) — hoher Nutzen, aber eigenständige Rollen-/Datenmodell-Changes.
