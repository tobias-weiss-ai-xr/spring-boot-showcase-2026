import { test, expect, LoginPage, RegisterPage, loginAsFarmer, loginAsAssessor } from '../fixtures';
import { uniqueEmail } from '../helpers/api';

test.describe('Authentication', () => {
  test('logs an existing FARMER in and lands on the farmer overview section', async ({ page }) => {
    const login = new LoginPage(page);
    await login.goto();
    await login.loginAs('max@bauernhof.de', 'passwort123');
    await expect(page).toHaveURL(/\/farmer\/uebersicht$/);
    await expect(page.getByRole('heading', { name: /Mein Betrieb/ })).toBeVisible();
    await expect(page.getByText(/Max Mustermann/).first()).toContainText('FARMER');
  });

  test('logs an ASSESSOR in and lands on the assessor Aufgaben section', async ({ page }) => {
    const login = new LoginPage(page);
    await login.goto();
    await login.loginAs('lisa@cropguard.de', 'assessor123');
    await expect(page).toHaveURL(/\/assessor\/aufgaben$/);
    await expect(page.getByRole('heading', { name: 'Gutachter-Portal' })).toBeVisible();
    await expect(page.getByText(/Lisa Gutachter/).first()).toContainText('ASSESSOR');
  });

  test('rejects wrong credentials with an error message', async ({ page }) => {
    const login = new LoginPage(page);
    await login.goto();
    await login.loginAs('max@bauernhof.de', 'falsches-passwort');
    await expect(page.getByRole('alert')).toContainText('Invalid email or password');
    await expect(page).toHaveURL(/\/login$/);
  });

  test('registers a new farmer through the UI and signs them in', async ({ page }) => {
    const email = uniqueEmail();
    const register = new RegisterPage(page);
    await register.goto();
    await register.registerFarmer('Neue Bäuerin', email, 'geheim123');
    await expect(page).toHaveURL(/\/farmer\/uebersicht$/);
    await expect(page.getByRole('heading', { name: /Mein Betrieb/ })).toBeVisible();
  });

  test('portal nav switches sections and legacy /farmer deep link redirects to the first section', async ({ page }) => {
    await loginAsFarmer(page, 'max@bauernhof.de', 'passwort123');

    const nav = page.getByRole('navigation', { name: 'Portal-Navigation' });
    await nav.getByRole('link', { name: 'Verträge' }).click();
    await expect(page).toHaveURL(/\/farmer\/vertraege$/);
    await nav.getByRole('link', { name: 'Lage' }).click();
    await expect(page).toHaveURL(/\/farmer\/lage$/);

    await page.goto('/farmer'); // legacy entry route
    await expect(page).toHaveURL(/\/farmer\/uebersicht$/);
  });

  test('blocks a FARMER from the assessor area', async ({ page, newFarmer }) => {
    await loginAsFarmer(page, newFarmer.email);
    await page.goto('/assessor');
    await expect(page.getByText('Zugriff verweigert')).toBeVisible();
  });

  test('blocks an ASSESSOR from the farmer area', async ({ page }) => {
    await loginAsAssessor(page); // awaits /assessor/aufgaben before deep-linking
    await page.goto('/farmer');
    await expect(page.getByText('Zugriff verweigert')).toBeVisible();
  });
});
