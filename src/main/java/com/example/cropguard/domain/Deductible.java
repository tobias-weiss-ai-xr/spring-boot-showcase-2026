package com.example.cropguard.domain;

/**
 * Catalog key for deductible options. The payout percentage and premium factor
 * live in the YAML rating packs, resolved by {@code RatingPackService}.
 */
public enum Deductible {
    NONE,
    FIVE_PERCENT,
    TEN_PERCENT,
    FIFTEEN_PERCENT,
    TWENTY_PERCENT
}
