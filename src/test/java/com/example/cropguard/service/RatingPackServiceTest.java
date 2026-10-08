package com.example.cropguard.service;

import com.example.cropguard.domain.Bundesland;
import com.example.cropguard.domain.CropType;
import com.example.cropguard.domain.Deductible;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for YAML rating pack loading, validation and country override.
 */
class RatingPackServiceTest {

    /** Loads the real packs from classpath:rating/ — as the application would at startup. */
    private RatingPackService realService() {
        return new RatingPackService();
    }

    @Test
    void defaultPackLookup() {
        RatingPackService service = realService();

        assertEquals(45.0, service.baseRateFor(CropType.WHEAT, "DE"), 0.0001);
        assertEquals(CropType.RiskCategory.LOW, service.riskCategoryFor(CropType.WHEAT, "DE"));
        assertEquals(CropType.RiskCategory.HIGH, service.riskCategoryFor(CropType.VINES, "DE"));
        assertEquals(1.5, service.riskFactorFor(Bundesland.BAYERN, "DE"), 0.0001);
        assertEquals(0.85, service.riskFactorFor(Bundesland.NIEDERSACHSEN, "DE"), 0.0001);
        assertEquals(0.92, service.premiumFactorFor(Deductible.FIVE_PERCENT, "DE"), 0.0001);
        assertEquals(10.0, service.percentageFor(Deductible.TEN_PERCENT, "DE"), 0.0001);
    }

    @Test
    void countryOverrideWins() {
        RatingPackService service = realService();

        // PL overrides WHEAT: higher base rate and MODERATE category
        assertEquals(48.0, service.baseRateFor(CropType.WHEAT, "PL"), 0.0001);
        assertEquals(CropType.RiskCategory.MODERATE, service.riskCategoryFor(CropType.WHEAT, "PL"));
        assertEquals(0.90, service.premiumFactorFor(Deductible.FIVE_PERCENT, "PL"), 0.0001);

        // DE values unchanged
        assertEquals(45.0, service.baseRateFor(CropType.WHEAT, "DE"), 0.0001);

        // PL entries not overridden inherit the DE base pack
        assertEquals(1.5, service.riskFactorFor(Bundesland.BAYERN, "PL"), 0.0001);

        // Unknown country falls back to DE
        assertEquals(45.0, service.baseRateFor(CropType.WHEAT, "FR"), 0.0001);
    }

    @Test
    void invalidPackFailsStartup_unknownKey() {
        Map<String, Object> badCrop = new HashMap<>();
        badCrop.put("baseRateEurPerHa", 45.0);
        badCrop.put("riskCategory", "LOW");
        Map<String, Object> crops = new HashMap<>();
        crops.put("SPELT", badCrop); // not a CropType constant
        Map<String, Object> root = new HashMap<>();
        root.put("country", "DE");
        root.put("crops", crops);

        Map<String, Map<String, Object>> packs = new HashMap<>();
        packs.put("default.yml", root);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> new RatingPackService(packs));
        assertTrue(ex.getMessage().contains("SPELT"), "should name the unknown key: " + ex.getMessage());
    }

    @Test
    void invalidPackFailsStartup_nonPositiveValue() {
        Map<String, Object> badBundesland = new HashMap<>();
        badBundesland.put("BAYERN", 0.0); // non-positive risk factor
        Map<String, Object> root = new HashMap<>();
        root.put("country", "DE");
        root.put("bundeslaender", badBundesland);

        Map<String, Map<String, Object>> packs = new HashMap<>();
        packs.put("default.yml", root);

        assertThrows(IllegalStateException.class, () -> new RatingPackService(packs));
    }

    @Test
    void invalidPackFailsStartup_missingBaseEntry() {
        Map<String, Object> root = new HashMap<>();
        root.put("country", "DE");
        root.put("crops", new HashMap<>()); // base pack without any crops
        root.put("bundeslaender", new HashMap<>());
        root.put("deductibles", new HashMap<>());

        Map<String, Map<String, Object>> packs = new HashMap<>();
        packs.put("default.yml", root);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> new RatingPackService(packs));
        assertTrue(ex.getMessage().contains("missing"), ex.getMessage());
    }

    @Test
    void invalidOverridePackFailsStartup() {
        Map<String, Object> override = new Yaml().load(
            "country: PL\n"
            + "crops:\n"
            + "  WHEAT:\n"
            + "    baseRateEurPerHa: -48.0\n"
            + "    riskCategory: MODERATE\n");
        Map<String, Map<String, Object>> packs = new HashMap<>();
        packs.put("default.yml", new Yaml().load(
            "country: DE\n"
            + "crops:\n"
            + "  WHEAT: {baseRateEurPerHa: 45.0, riskCategory: LOW}\n"
            + "bundeslaender: {BAYERN: 1.5}\n"
            + "deductibles: {NONE: {percentage: 0.0, premiumFactor: 1.0}}\n"));
        packs.put("pl.yml", override);

        assertThrows(IllegalStateException.class, () -> new RatingPackService(packs));
    }
}
