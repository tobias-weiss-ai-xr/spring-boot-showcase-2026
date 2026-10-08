package com.example.cropguard.domain;

/**
 * Catalog key for German federal states. The numeric risk factor lives in the
 * YAML rating packs, resolved by {@code RatingPackService}.
 */
public enum Bundesland {
    BAYERN("Bayern"),
    BADEN_WUERTTEMBERG("Baden-Wuerttemberg"),
    HESSEN("Hessen"),
    RHEINLAND_PFALZ("Rheinland-Pfalz"),
    SAARLAND("Saarland"),
    THUERINGEN("Thueringen"),
    SACHSEN("Sachsen"),
    SACHSEN_ANHALT("Sachsen-Anhalt"),
    NORDRHEIN_WESTFALEN("Nordrhein-Westfalen"),
    NIEDERSACHSEN("Niedersachsen"),
    SCHLESWIG_HOLSTEIN("Schleswig-Holstein"),
    MECKLENBURG_VORPOMMERN("Mecklenburg-Vorpommern"),
    BRANDENBURG("Brandenburg"),
    BERLIN("Berlin"),
    BREMEN("Bremen"),
    HAMBURG("Hamburg");

    private final String name;

    Bundesland(String name) {
        this.name = name;
    }

    public String getName() { return name; }
}
