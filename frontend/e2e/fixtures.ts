import { test as base, expect, Page, APIRequestContext } from '@playwright/test';

import { LoginPage, RegisterPage, FarmerDashboard, AssessorDashboard } from './pages';
import { registerFarmer, uniqueEmail } from './helpers/api';

/**
 * Extended fixtures:
 *  - api:      request context for backend seeding
 *  - seedApi:  registers a fresh FARMER via the API for this test
 *  - farmerUi: a fully-authenticated, fresh farmer's page (UI login, alerts auto-accepted)
 */
export const test = base.extend<{
  api: APIRequestContext;
  newFarmer: { email: string; password: string };
}>({
  api: async ({ playwright }, use) => {
    const ctx = await playwright.request.newContext();
    await use(ctx);
    await ctx.dispose();
  },
  newFarmer: async ({ api }, use) => {
    const email = await registerFarmer(api, uniqueEmail());
    await use({ email, password: 'geheim123' });
  },
});

export async function loginAsFarmer(page: Page, email: string, password = 'geheim123'): Promise<FarmerDashboard> {
  page.on('dialog', d => void d.accept()); // CropGuard confirms mutations with window.alert
  const login = new LoginPage(page);
  await login.goto();
  await login.loginAs(email, password);
  await expect(page).toHaveURL(/\/farmer$/);
  return new FarmerDashboard(page);
}

export async function loginAsAssessor(page: Page): Promise<AssessorDashboard> {
  page.on('dialog', d => void d.accept());
  const login = new LoginPage(page);
  await login.goto();
  await login.loginAs('lisa@cropguard.de', 'assessor123');
  await expect(page).toHaveURL(/\/assessor$/);
  return new AssessorDashboard(page);
}

export { expect, LoginPage, RegisterPage, FarmerDashboard, AssessorDashboard };
