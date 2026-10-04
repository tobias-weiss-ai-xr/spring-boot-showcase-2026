package com.example.cropguard.domain;

public enum Bundesland {
    BAYERN(1.5, "Bayern"),
    BADEN_WUERTTEMBERG(1.4, "Baden-Wuerttemberg"),
    HESSEN(1.0, "Hessen"),
    RHEINLAND_PFALZ(1.1, "Rheinland-Pfalz"),
    SAARLAND(1.1, "Saarland"),
    THUERINGEN(1.1, "Thueringen"),
    SACHSEN(1.1, "Sachsen"),
    SACHSEN_ANHALT(1.0, "Sachsen-Anhalt"),
    NORDRHEIN_WESTFALEN(1.0, "Nordrhein-Westfalen"),
    NIEDERSACHSEN(0.85, "Niedersachsen"),
    SCHLESWIG_HOLSTEIN(0.8, "Schleswig-Holstein"),
    MECKLENBURG_VORPOMMERN(0.8, "Mecklenburg-Vorpommern"),
    BRANDENBURG(0.9, "Brandenburg"),
    BERLIN(0.9, "Berlin"),
    BREMEN(0.85, "Bremen"),
    HAMBURG(0.85, "Hamburg");

    private final double riskFactor;
    private final String name;

    Bundesland(double riskFactor, String name) {
        this.riskFactor = riskFactor;
        this.name = name;
    }

    public double getRiskFactor() { return riskFactor; }
    public String getName() { return name; }
}
