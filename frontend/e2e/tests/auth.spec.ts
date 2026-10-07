import { test, expect, LoginPage, RegisterPage } from '../fixtures';
import { loginAsAssessor, loginAsFarmer } from '../fixtures';
import { uniqueEmail } from '../helpers/api';

test.describe('Authentication', () => {
  test('logs an existing FARMER in and lands on the farmer dashboard', async ({ page }) => {
    const login = new LoginPage(page);
    await login.goto();
    await login.loginAs('max@bauernhof.de', 'passwort123');
    await expect(page).toHaveURL(/\/farmer$/);
    await expect(page.getByRole('heading', { name: /Mein Betrieb/ })).toBeVisible();
    await expect(page.getByText(/Max Mustermann/).first()).toContainText('FARMER');
  });

  test('logs an ASSESSOR in and lands on the assessor dashboard', async ({ page }) => {
    await loginAsAssessor(page);
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
    await expect(page).toHaveURL(/\/farmer$/);
    await expect(page.getByRole('heading', { name: /Mein Betrieb/ })).toBeVisible();
  });

  test('blocks a FARMER from the assessor area', async ({ page, newFarmer }) => {
    await loginAsFarmer(page, newFarmer.email);
    await page.goto('/assessor');
    await expect(page.getByText('Zugriff verweigert')).toBeVisible();
  });

  test('blocks an ASSESSOR from the farmer area', async ({ page }) => {
    await loginAsAssessor(page);
    await page.goto('/farmer');
    await expect(page.getByText('Zugriff verweigert')).toBeVisible();
  });
});
