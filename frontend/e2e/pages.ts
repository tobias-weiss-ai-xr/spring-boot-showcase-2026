import { Page } from 'playwright';

/** Page objects — CropGuard UI (German labels). */

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

export class FarmerDashboard {
  constructor(readonly page: Page) {}

  readonly heading = this.page.getByRole('heading', { name: /Mein Betrieb/ });

  // Quote
  readonly cropType = this.page.getByLabel('Kultur').first();
  readonly quotePremium = this.page.locator('.kv dd').first();

  // Plot form
  readonly plotCropType = this.page.getByLabel('Kultur').nth(1);
  readonly plotHectares = this.page.getByLabel('Hektar').first();
  readonly plotBundesland = this.page.getByLabel('Bundesland').nth(1);
  readonly plotE = this.page.getByLabel('GK3 E').first();
  readonly plotN = this.page.getByLabel('GK3 N').first();
  readonly plotDescription = this.page.getByLabel('Beschreibung').first();
  readonly createPlotButton = this.page.getByRole('button', { name: 'Feld anlegen' });

  // Policy form
  readonly plotSelect = this.page.getByLabel('Feld');
  readonly coverage = this.page.getByLabel('Deckung (€)');
  readonly coverageStart = this.page.getByLabel('Beginn');
  readonly coverageEnd = this.page.getByLabel('Ende');
  readonly buyPolicyButton = this.page.getByRole('button', { name: 'Police abschließen' });

  // Claim form
  readonly policySelect = this.page.getByLabel('Police');
  readonly damageDate = this.page.getByLabel('Schadensdatum');
  readonly damageDescription = this.page.getByLabel('Beschreibung').nth(1);
  readonly fileClaimButton = this.page.getByRole('button', { name: 'Schaden melden' });

  // Tables
  readonly policyRows = this.page.locator('section', { hasText: 'Meine Policen' }).locator('tbody tr');
  readonly claimRows = this.page.locator('section', { hasText: 'Meine Schäden' }).locator('tbody tr');
}

export class AssessorDashboard {
  constructor(readonly page: Page) {}

  readonly heading = this.page.getByRole('heading', { name: 'Gutachter-Portal' });

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

  readonly hailEventsSection = this.page.locator('section', { hasText: 'Hagelereignisse' });
}
