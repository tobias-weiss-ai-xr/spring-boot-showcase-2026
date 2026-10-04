package com.example.cropguard.domain;

public enum Deductible {
    NONE(0.0, 1.0),
    FIVE_PERCENT(5.0, 0.92),
    TEN_PERCENT(10.0, 0.85),
    FIFTEEN_PERCENT(15.0, 0.78),
    TWENTY_PERCENT(20.0, 0.72);

    private final double percentage;
    private final double premiumFactor;

    Deductible(double percentage, double premiumFactor) {
        this.percentage = percentage;
        this.premiumFactor = premiumFactor;
    }

    public double getPercentage() { return percentage; }
    public double getPremiumFactor() { return premiumFactor; }
}
