package com.example.cropguard.service;

import com.example.cropguard.domain.Bundesland;
import com.example.cropguard.domain.CropType;
import com.example.cropguard.domain.Deductible;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DWD drought index grid lookup and premium calculation with drought adjustment.
 * Uses the real DWD multi-annual drought index grid (July, 1991-2020) bundled as a classpath resource.
 */
class DwdRiskGridServiceTest {

    private DwdRiskGridService createService() {
        DwdRiskGridService service = new DwdRiskGridService();
        service.load();
        return service;
    }

    @Test
    void gridLoadsSuccessfully() {
        DwdRiskGridService service = createService();
        // Kassel area (within grid)
        assertTrue(service.isCovered(3533000, 5690000));
        // Outside grid
        assertFalse(service.isCovered(0, 0));
        assertFalse(service.isCovered(1000000, 4000000));
    }

    @Test
    void droughtIndexAtKassel() {
        DwdRiskGridService service = createService();
        int index = service.getDroughtIndex(3533000, 5690000);
        assertTrue(index >= 1 && index <= 26, "Drought index should be 1-26, got: " + index);
        assertEquals(2, index, "Kassel area should have drought index 2 (low drought)");
        assertEquals(1.0, service.getDroughtAdjustment(3533000, 5690000), 0.001,
            "Drought adjustment for index 2 should be 1.0 (baseline)");
    }

    @Test
    void droughtIndexOutsideGrid() {
        DwdRiskGridService service = createService();
        assertEquals(0, service.getDroughtIndex(0, 0));
        assertEquals(1.0, service.getDroughtAdjustment(0, 0), 0.001,
            "Drought adjustment outside grid should be 1.0 (no adjustment)");
    }

    @Test
    void premiumIncludesDroughtAdjustment() {
        DwdRiskGridService grid = createService();
        PremiumCalculator calc = new PremiumCalculator(grid);

        // Kassel area: drought index 2, adjustment 1.0 (baseline)
        var result = calc.calculate(CropType.WHEAT, 25.0, Bundesland.HESSEN,
            Deductible.TEN_PERCENT, 25000.0, 3533000.0, 5690000.0);

        // base = 45 * 25 = 1125
        // bundeslandFactor = 1.0 (HESSEN)
        // droughtAdj = 1.0 (index 2)
        // riskAdjusted = 1125 * 1.0 * 1.0 = 1125
        // deductibleAdjusted = 1125 * 0.85 = 956.25
        assertEquals(956.25, result.premiumEur(), 0.01);
        assertEquals(2, result.droughtIndex());
        assertEquals(1.0, result.droughtAdjustment(), 0.001);
        assertEquals(1.0, result.riskAdjustment(), 0.001);
    }

    @Test
    void premiumWithHigherDroughtIndex() {
        DwdRiskGridService grid = createService();
        PremiumCalculator calc = new PremiumCalculator(grid);

        // Munich area: drought index 3, adjustment 1.05
        var result = calc.calculate(CropType.WHEAT, 25.0, Bundesland.BAYERN,
            Deductible.TEN_PERCENT, 25000.0, 3700000.0, 5570000.0);

        // base = 45 * 25 = 1125
        // bundeslandFactor = 1.5 (BAYERN)
        // droughtAdj = 1.05 (index 3)
        // riskAdjusted = 1125 * 1.5 * 1.05 = 1771.875
        // deductibleAdjusted = 1771.875 * 0.85 = 1506.09375
        assertEquals(1506.09, result.premiumEur(), 0.01);
        assertEquals(3, result.droughtIndex());
        assertEquals(1.05, result.droughtAdjustment(), 0.001);
        assertEquals(1.575, result.riskAdjustment(), 0.001); // 1.5 * 1.05
    }

    @Test
    void premiumWithoutCoordinatesSkipsDroughtLookup() {
        DwdRiskGridService grid = createService();
        PremiumCalculator calc = new PremiumCalculator(grid);

        // No coordinates — drought lookup skipped, adjustment = 1.0
        var result = calc.calculate(CropType.WHEAT, 25.0, Bundesland.HESSEN,
            Deductible.TEN_PERCENT, 25000.0, null, null);

        assertEquals(956.25, result.premiumEur(), 0.01);
        assertEquals(0, result.droughtIndex());
        assertEquals(1.0, result.droughtAdjustment(), 0.001);
    }
}
