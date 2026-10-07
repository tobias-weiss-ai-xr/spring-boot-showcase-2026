import { test, expect } from '../fixtures';
import { loginAsFarmer } from '../fixtures';

test.describe('Farmer dashboard', () => {
  test('quote recalculates when the crop changes', async ({ page, newFarmer }) => {
    const farmer = await loginAsFarmer(page, newFarmer.email);

    await farmer.cropType.selectOption('BARLEY'); // triggers a fresh quote (live recalculation)
    await expect(farmer.quotePremium).toBeVisible();
    expect(farmer.quotePremium).not.toBeNull();

    const before = await farmer.quotePremium.textContent();
    await farmer.cropType.selectOption('HOPS'); // highest-risk crop -> premium must rise
    await expect(farmer.quotePremium).not.toHaveText(before as string);
    await expect(farmer.quotePremium).toContainText('€');
  });

  test('full insurance lifecycle: plot → policy → claim', async ({ page, newFarmer }) => {
    const farmer = await loginAsFarmer(page, newFarmer.email);
    await expect(farmer.policyRows).toHaveCount(0); // a fresh farmer starts empty

    // 1. create a plot
    await farmer.plotDescription.fill('Mein Weizenfeld in Nordhessen');
    await farmer.createPlotButton.click();
    await expect(farmer.plotSelect).not.toBeDisabled();

    // 2. buy a policy for it
    await farmer.coverageStart.fill('2026-03-01');
    await farmer.coverageEnd.fill('2026-12-31');
    await farmer.buyPolicyButton.click();
    await expect(farmer.policyRows).toHaveCount(1);
    await expect(farmer.policyRows.first()).toContainText('ACTIVE');
    await expect(farmer.policyRows.first()).toContainText('25');

    // 3. file a claim under that policy
    await farmer.damageDate.fill('2026-07-15');
    await farmer.damageDescription.fill('Vollständiger Ernteverlust durch Hagel');
    await farmer.fileClaimButton.click();
    await expect(farmer.claimRows).toHaveCount(1);
    await expect(farmer.claimRows.first()).toContainText('Vollständiger Ernteverlust durch Hagel');
    await expect(farmer.claimRows.first()).toContainText('SUBMITTED');
  });
});
