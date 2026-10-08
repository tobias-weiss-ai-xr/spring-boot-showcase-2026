import { test as base, expect, Page, APIRequestContext } from '@playwright/test';

import {
  LoginPage,
  RegisterPage,
  FarmerOverview,
  FarmerAnbau,
  FarmerVertraege,
  FarmerSchaeden,
  FarmerLage,
  AssessorAufgaben,
  AssessorLagebild,
} from './pages';
import { registerFarmer, uniqueEmail } from './helpers/api';

/**
 * Extended fixtures:
 *  - api:      request context for backend seeding
 *  - newFarmer:registers a fresh FARMER via the API for this test
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

export async function loginAsFarmer(page: Page, email: string, password = 'geheim123'): Promise<Page> {
  page.on('dialog', d => void d.accept()); // CropGuard confirms mutations with window.alert
  const login = new LoginPage(page);
  await login.goto();
  await login.loginAs(email, password);
  await expect(page).toHaveURL(/\/farmer\/uebersicht$/); // /farmer redirects to the first section
  return page;
}

export async function loginAsAssessor(page: Page): Promise<Page> {
  page.on('dialog', d => void d.accept());
  const login = new LoginPage(page);
  await login.goto();
  await login.loginAs('lisa@cropguard.de', 'assessor123');
  await expect(page).toHaveURL(/\/assessor\/aufgaben$/); // /assessor redirects to the first section
  return page;
}

export {
  expect,
  LoginPage,
  RegisterPage,
  FarmerOverview,
  FarmerAnbau,
  FarmerVertraege,
  FarmerSchaeden,
  FarmerLage,
  AssessorAufgaben,
  AssessorLagebild,
};
