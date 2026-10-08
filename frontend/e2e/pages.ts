import { Page } from 'playwright';

/** Page objects — CropGuard UI (German labels), one class per portal section (/farmer/*, /assessor/*). */

export class LoginPage {
  constructor(readonly page: Page) {}

  async goto(): Promise<void> {
    await this.page.goto('/login');
  }

  async loginAs(email: string, password: string): Promise<void> {
    await this.page.getByLabel('E-Mail').fill(email);
    await this.page.getByLabel('Passwort').fill(password);
    await this.page.getByRole('button', { name: 'Anmelden' }).click();
  }

  readonly errorBox = () => this.page.getByRole('alert');
}

export class RegisterPage {
  constructor(readonly page: Page) {}

  async goto(): Promise<void> {
    await this.page.goto('/register');
  }

  async registerFarmer(name: string, email: string, password: string): Promise<void> {
    await this.page.getByLabel('Name').fill(name);
    await this.page.getByLabel('E-Mail').fill(email);
    await this.page.getByLabel('Passwort').fill(password);
    await this.page.getByLabel('Bundesland').selectOption('HESSEN');
    await this.page.getByRole('button', { name: 'Registrieren' }).click();
  }
}

/** /farmer/uebersicht — quote calculator. */
export class FarmerOverview {
  constructor(readonly page: Page) {}

  readonly cropType = this.page.getByLabel('Kultur');
  readonly quotePremium = this.page.locator('.kv dd').first();
}

/** /farmer/anbau — plot create form + plot table. */
export class FarmerAnbau {
  constructor(readonly page: Page) {}

  readonly cropType = this.page.getByLabel('Kultur');
  readonly hectares = this.page.getByLabel('Hektar');
  readonly bundesland = this.page.getByLabel('Bundesland');
  readonly coordinateE = this.page.getByLabel('GK3 E');
  readonly coordinateN = this.page.getByLabel('GK3 N');
  readonly description = this.page.getByLabel('Beschreibung');
  readonly createPlotButton = this.page.getByRole('button', { name: 'Feld anlegen' });
  readonly plotRows = this.page.locator('section', { hasText: 'Meine Felder' }).locator('tbody tr');
}

/** /farmer/vertraege — policy create form + policy table. */
export class FarmerVertraege {
  constructor(readonly page: Page) {}

  readonly plotSelect = this.page.getByLabel('Feld', { exact: true });
  readonly coverage = this.page.getByLabel('Deckung (€)');
  readonly coverageStart = this.page.getByLabel('Beginn');
  readonly coverageEnd = this.page.getByLabel('Ende');
  readonly buyPolicyButton = this.page.getByRole('button', { name: 'Police abschließen' });
  readonly policyRows = this.page.locator('section', { hasText: 'Meine Policen' }).locator('tbody tr');
}

/** /farmer/schaeden — claim form + claim table. */
export class FarmerSchaeden {
  constructor(readonly page: Page) {}

  readonly policySelect = this.page.getByLabel('Police');
  readonly damageDate = this.page.getByLabel('Schadensdatum');
  readonly damageDescription = this.page.getByLabel('Beschreibung');
  readonly fileClaimButton = this.page.getByRole('button', { name: 'Schaden melden' });
  readonly claimRows = this.page.locator('section', { hasText: 'Meine Schäden' }).locator('tbody tr');
}

/** /farmer/lage — DWD risk map. */
export class FarmerLage {
  constructor(readonly page: Page) {}

  readonly canvas = this.page.locator('canvas');
  readonly legend = this.page.locator('.legend');
}

/** /assessor/aufgaben — claim workbench queue + assess panel. */
export class AssessorAufgaben {
  constructor(readonly page: Page) {}

  claimRow(description: string) {
    return this.page.getByRole('row', { name: new RegExp(description) });
  }

  async assess(description: string, damagePercent: number, decision: string, notes: string): Promise<void> {
    const row = this.claimRow(description);
    await row.getByRole('button', { name: 'Bearbeiten' }).click();
    await this.page.getByLabel('Schaden (%)').fill(String(damagePercent));
    await this.page.getByLabel('Entscheidung').selectOption(decision);
    await this.page.getByLabel('Anmerkungen').fill(notes);
    await this.page.getByRole('button', { name: 'Entscheidung speichern' }).click();
  }
}

/** /assessor/lagebild — hail-event feed + risk map. */
export class AssessorLagebild {
  constructor(readonly page: Page) {}

  readonly hailEventsSection = this.page.locator('section', { hasText: 'Hagelereignisse' });
}
