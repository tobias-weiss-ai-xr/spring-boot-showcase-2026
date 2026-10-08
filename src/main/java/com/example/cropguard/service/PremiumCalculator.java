package com.example.cropguard.service;

import com.example.cropguard.domain.Bundesland;
import com.example.cropguard.domain.CropType;
import com.example.cropguard.domain.Deductible;
import org.springframework.stereotype.Service;

/**
 * The core domain logic: premium calculation for hail insurance.
 *
 * Formula:
 *   base = ratingPacks.baseRateFor(cropType) × hectares
 *   riskAdjusted = base × ratingPacks.riskFactorFor(bundesland)
 *   deductibleAdjusted = riskAdjusted × ratingPacks.premiumFactorFor(deductible)
 *   premium = round(deductibleAdjusted, 2 decimal places)
 *
 * All rating values are resolved through {@link RatingPackService} (default
 * country DE); see src/main/resources/rating/*.yml.
 *
 * Business rules:
 *   - Minimum premium: 50 EUR (admin fee for very small fields)
 *   - Maximum coverage: 500,000 EUR per plot
 *   - Coverage must be ≥ premium × 10 (insurance principle)
 */
@Service
public class PremiumCalculator {

    private static final double MIN_PREMIUM_EUR = 50.0;
    private static final double MAX_COVERAGE_EUR = 500_000.0;
    private static final double MIN_COVERAGE_TO_PREMIUM_RATIO = 10.0;

    private final DwdRiskGridService dwdRiskGridService;
    private final RatingPackService ratingPacks;

    public PremiumCalculator(DwdRiskGridService dwdRiskGridService, RatingPackService ratingPacks) {
        this.dwdRiskGridService = dwdRiskGridService;
        this.ratingPacks = ratingPacks;
    }

    public record PremiumResult(
        double premiumEur,
        double baseEur,
        double riskAdjustment,
        double deductibleAdjustment,
        double coverageEur,
        int droughtIndex,
        double droughtAdjustment
    ) {}

    /**
     * Calculate premium for a plot.
     *
     * @param cropType   crop type with base rate
     * @param hectares   field size in hectares
     * @param bundesland     German Bundesland with risk factor
     * @param deductible deductible option
     * @param coverageEur desired coverage amount in EUR
     * @param coordinateE  GK3 easting (nullable — DWD drought lookup skipped if null)
     * @param coordinateN  GK3 northing (nullable)
     * @return premium result with breakdown
     */
    public PremiumResult calculate(CropType cropType, double hectares, Bundesland bundesland,
                                   Deductible deductible, double coverageEur,
                                   Double coordinateE, Double coordinateN) {
        validate(hectares, coverageEur);

        String country = RatingPackService.DEFAULT_COUNTRY;

        double base = ratingPacks.baseRateFor(cropType, country) * hectares;
        double bundeslandFactor = ratingPacks.riskFactorFor(bundesland, country);

        int droughtIndex = 0;
        double droughtAdj = 1.0;
        if (coordinateE != null && coordinateN != null) {
            droughtIndex = dwdRiskGridService.getDroughtIndex(coordinateE, coordinateN);
            droughtAdj = dwdRiskGridService.getDroughtAdjustment(coordinateE, coordinateN);
        }

        double riskAdjusted = base * bundeslandFactor * droughtAdj;
        double premiumFactor = ratingPacks.premiumFactorFor(deductible, country);
        double deductibleAdjusted = riskAdjusted * premiumFactor;
        double premium = Math.max(MIN_PREMIUM_EUR, Math.round(deductibleAdjusted * 100.0) / 100.0);

        return new PremiumResult(
            premium,
            Math.round(base * 100.0) / 100.0,
            bundeslandFactor * droughtAdj,
            premiumFactor,
            coverageEur,
            droughtIndex,
            droughtAdj
        );
    }

    /** Calculate payout for a claim based on damage percentage and deductible. */
    public double calculatePayout(double coverageEur, double damagePercent, Deductible deductible) {
        if (damagePercent < 0 || damagePercent > 100) {
            throw new IllegalArgumentException("Damage percent must be 0–100, got: " + damagePercent);
        }
        double rawPayout = coverageEur * (damagePercent / 100.0);
        double deductibleAmount = rawPayout * (ratingPacks.percentageFor(deductible, RatingPackService.DEFAULT_COUNTRY) / 100.0);
        double payout = rawPayout - deductibleAmount;
        return Math.round(payout * 100.0) / 100.0;
    }

    private void validate(double hectares, double coverageEur) {
        if (hectares < 0.1) {
            throw new IllegalArgumentException("Field must be at least 0.1 hectares");
        }
        if (coverageEur < 1000) {
            throw new IllegalArgumentException("Coverage must be at least 1,000 EUR");
        }
        if (coverageEur > MAX_COVERAGE_EUR) {
            throw new IllegalArgumentException("Coverage cannot exceed 500,000 EUR per plot");
        }
    }

    public double getMinPremiumEur() { return MIN_PREMIUM_EUR; }
    public double getMaxCoverageEur() { return MAX_COVERAGE_EUR; }
}
