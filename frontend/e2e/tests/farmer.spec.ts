import { test, expect, loginAsFarmer } from '../fixtures';
import {
  FarmerOverview,
  FarmerAnbau,
  FarmerVertraege,
  FarmerSchaeden,
  FarmerLage,
} from '../pages';
import { login, seedPolicy } from '../helpers/api';

test.describe('Farmer portal', () => {
  test('quote recalculates when the crop changes', async ({ page, newFarmer }) => {
    await loginAsFarmer(page, newFarmer.email);
    const overview = new FarmerOverview(page);

    await overview.cropType.selectOption('BARLEY'); // triggers a fresh quote (live recalculation)
    await expect(overview.quotePremium).toBeVisible();

    const before = await overview.quotePremium.textContent();
    await overview.cropType.selectOption('HOPS'); // highest-risk crop -> premium must rise
    await expect(overview.quotePremium).not.toHaveText(before as string);
    await expect(overview.quotePremium).toContainText('€');
  });

  test('full insurance lifecycle across sections: plot (Anbau) → policy (Verträge) → claim (Schäden)', async ({ page, newFarmer }) => {
    await loginAsFarmer(page, newFarmer.email);

    // 1. create a plot in the Anbau section
    await page.goto('/farmer/anbau');
    const anbau = new FarmerAnbau(page);
    await anbau.description.fill('Mein Weizenfeld in Nordhessen');
    await anbau.createPlotButton.click();
    await expect(anbau.plotRows).toHaveCount(1);

    // 2. buy a policy for it in the Verträge section
    await page.goto('/farmer/vertraege');
    const vertraege = new FarmerVertraege(page);
    await expect(vertraege.policyRows).toHaveCount(0); // a fresh farmer starts empty
    await vertraege.coverageStart.fill('2026-03-01');
    await vertraege.coverageEnd.fill('2026-12-31');
    await vertraege.buyPolicyButton.click();
    await expect(vertraege.policyRows).toHaveCount(1);
    await expect(vertraege.policyRows.first()).toContainText('ACTIVE');
    await expect(vertraege.policyRows.first()).toContainText('25');

    // 3. file a claim through the FNOL wizard in the Schäden section
    await page.goto('/farmer/schaeden');
    const schaeden = new FarmerSchaeden(page);
    await expect(schaeden.stepPill(1)).toContainText('Feld'); // step indicator starts at 1
    await schaeden.nextButton.click(); // step 1: first policy preselected
    await schaeden.damageDate.fill('2026-07-15');
    await schaeden.nextButton.click();
    await schaeden.damageDescription.fill('Vollständiger Ernteverlust durch Hagel');
    await schaeden.nextButton.click();
    // summary step shows the entered values read-only before confirming
    await expect(schaeden.summary).toContainText('15.07.2026');
    await expect(schaeden.summary).toContainText('Vollständiger Ernteverlust durch Hagel');
    await schaeden.fileClaimButton.click();
    await expect(schaeden.claimRows).toHaveCount(1);
    await expect(schaeden.claimRows.first()).toContainText('Vollständiger Ernteverlust durch Hagel');
    await expect(schaeden.claimRows.first()).toContainText('SUBMITTED');
  });

  test('FNOL wizard: step-back keeps entries, linked hail event shows up in the claim list', async ({ page, request }) => {
    const { email } = await seedPolicy(request); // plot + policy via API for a fresh farmer
    await loginAsFarmer(page, email);
    await page.goto('/farmer/schaeden');
    const schaeden = new FarmerSchaeden(page);

    await schaeden.nextButton.click();
    await schaeden.damageDate.fill('2026-07-15');
    await schaeden.hailEvent.selectOption({ index: 1 }); // the single seeded hail event
    await schaeden.backButton.click();
    await expect(schaeden.stepPill(1)).toContainText('Feld'); // back on step 1
    await schaeden.nextButton.click();
    await expect(schaeden.damageDate).toHaveValue('2026-07-15'); // entries survive the step-back
    await schaeden.nextButton.click();
    await schaeden.damageDescription.fill('Hagelschaden mit verknüpftem Ereignis');
    await schaeden.nextButton.click();
    await schaeden.fileClaimButton.click();

    await expect(schaeden.claimRows).toHaveCount(1);
    await expect(schaeden.claimRows.first()).toContainText('SEVERE'); // hail-event linkage column
  });

  test('farmer scoping: GET /api/claims never returns other farmers claims', async ({ page, request, newFarmer }) => {
    const { token } = await login(request, newFarmer.email);

    const res = await request.get('http://localhost:8080/api/claims', {
      headers: { Authorization: `Bearer ${token}` },
    });
    expect(res.ok()).toBeTruthy();
    const claims = await res.json();
    // the seeded demo claim belongs to max@bauernhof.de — a fresh farmer must not see it
    expect(claims.every((c: { damageDescription: string }) =>
      !c.damageDescription.includes('Vollstaendiger'))).toBe(true);
  });

  test('renders the DWD risk map with drawn grid cells', async ({ page, newFarmer }) => {
    await loginAsFarmer(page, newFarmer.email);
    await page.goto('/farmer/lage');
    const lage = new FarmerLage(page);

    await expect(lage.canvas).toBeVisible();

    // the grid must actually be painted (opaque pixels from the color scale)
    const opaquePixels = await lage.canvas.evaluate((cv: HTMLCanvasElement) => {
      const ctx = cv.getContext('2d');
      if (!ctx) return 0;
      const d = ctx.getImageData(0, 0, cv.width, cv.height).data;
      let n = 0;
      for (let i = 3; i < d.length; i += 4) if (d[i] > 0) n++;
      return n;
    });
    expect(opaquePixels).toBeGreaterThan(10000);
    await expect(lage.legend).toContainText('Trockenindex');
  });
});
