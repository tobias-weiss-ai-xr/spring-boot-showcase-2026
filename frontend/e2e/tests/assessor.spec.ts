import { test, expect, loginAsAssessor } from '../fixtures';
import { AssessorAufgaben, AssessorLagebild } from '../pages';
import { seedClaim } from '../helpers/api';

test.describe('Assessor portal', () => {
  test('assesses a farmer claim end-to-end (SUBMITTED → APPROVED)', async ({ page, api }) => {
    const { description } = await seedClaim(api);
    await loginAsAssessor(page);
    const aufgaben = new AssessorAufgaben(page);

    const row = aufgaben.claimRow(description);
    await expect(row).toContainText('SUBMITTED');

    await aufgaben.assess(description, 80, 'APPROVED', 'Totalverlust bestätigt');

    // Payout shows for an approved claim
    await expect(row).toContainText('APPROVED');
    await expect(row).toContainText('€');
  });

  test('lists registered hail events in the Lagebild section', async ({ page }) => {
    await loginAsAssessor(page);
    await page.goto('/assessor/lagebild');
    const lagebild = new AssessorLagebild(page);

    await expect(lagebild.hailEventsSection).toBeVisible();
    const rows = lagebild.hailEventsSection.locator('tbody tr');
    await expect(rows.first()).toContainText('SEVERE');
    await expect(rows.first()).toContainText('15.07.2026');
  });
});
