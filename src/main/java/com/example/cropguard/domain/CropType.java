package com.example.cropguard.domain;

/**
 * Catalog key for crop types. All numeric ratings (base rate, risk category)
 * live in the YAML rating packs, resolved by {@code RatingPackService}.
 */
public enum CropType {
    WHEAT,
    BARLEY,
    CORN,
    RAPESEED,
    VINES,
    APPLES,
    PEARS,
    CHERRIES,
    STRAWBERRIES,
    RASPBERRIES,
    POTATOES,
    VEGETABLES,
    TOBACCO,
    HOPS;

    /** Risk category vocabulary used by the rating packs. */
    public enum RiskCategory { LOW, MODERATE, HIGH }
}
