package com.example.cropguard.domain;

public enum CropType {
    WHEAT(45.0, RiskCategory.LOW),
    BARLEY(42.0, RiskCategory.LOW),
    CORN(38.0, RiskCategory.LOW),
    RAPESEED(50.0, RiskCategory.MODERATE),
    VINES(280.0, RiskCategory.HIGH),
    APPLES(180.0, RiskCategory.HIGH),
    PEARS(170.0, RiskCategory.HIGH),
    CHERRIES(220.0, RiskCategory.HIGH),
    STRAWBERRIES(200.0, RiskCategory.HIGH),
    RASPBERRIES(210.0, RiskCategory.HIGH),
    POTATOES(55.0, RiskCategory.MODERATE),
    VEGETABLES(90.0, RiskCategory.MODERATE),
    TOBACCO(120.0, RiskCategory.MODERATE),
    HOPS(350.0, RiskCategory.HIGH);

    public enum RiskCategory { LOW, MODERATE, HIGH }

    private final double baseRateEurPerHa;
    private final RiskCategory riskCategory;

    CropType(double baseRateEurPerHa, RiskCategory riskCategory) {
        this.baseRateEurPerHa = baseRateEurPerHa;
        this.riskCategory = riskCategory;
    }

    public double getBaseRateEurPerHa() { return baseRateEurPerHa; }
    public RiskCategory getRiskCategory() { return riskCategory; }
}
