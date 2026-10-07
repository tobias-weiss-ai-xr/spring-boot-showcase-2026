import { test, expect } from '../fixtures';
import { loginAsAssessor } from '../fixtures';
import { seedClaim } from '../helpers/api';

test.describe('Assessor dashboard', () => {
  test('assesses a farmer claim end-to-end (SUBMITTED → APPROVED)', async ({ page, api }) => {
    const { description } = await seedClaim(api);
    const assessor = await loginAsAssessor(page);

    const row = assessor.claimRow(description);
    await expect(row).toContainText('SUBMITTED');

    await assessor.assess(description, 80, 'APPROVED', 'Totalverlust bestätigt');

    // Payout shows for an approved claim
    await expect(row).toContainText('APPROVED');
    await expect(row).toContainText('€');
  });

  test('lists registered hail events', async ({ page }) => {
    const assessor = await loginAsAssessor(page);
    await expect(assessor.hailEventsSection).toBeVisible();
    const rows = assessor.hailEventsSection.locator('tbody tr');
    await expect(rows.first()).toContainText('SEVERE');
    await expect(rows.first()).toContainText('15.07.2026');
  });
});
