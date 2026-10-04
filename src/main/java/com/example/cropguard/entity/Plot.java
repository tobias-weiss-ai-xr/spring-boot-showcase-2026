package com.example.cropguard.entity;

import com.example.cropguard.domain.Bundesland;
import com.example.cropguard.domain.CropType;
import jakarta.persistence.*;

/** A field/plot insured against hail. Module 5: JPA entity with enum mappings. */
@Entity
@Table(name = "plots")
public class Plot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CropType cropType;

    @Column(nullable = false)
    private Double hectares;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Bundesland bundesland;

    /** UTM coordinates (E, N) — for geo-locating the plot. */
    @Column
    private Double coordinateE;

    @Column
    private Double coordinateN;

    /** Human-readable location, e.g. "Weinberg oberhalb Bingen, Rheinhessen". */
    @Column(nullable = false)
    private String locationDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insured_id", nullable = false)
    private Insured insured;

    protected Plot() {}

    public Plot(CropType cropType, Double hectares, Bundesland bundesland,
                Double coordinateE, Double coordinateN,
                String locationDescription, Insured insured) {
        this.cropType = cropType;
        this.hectares = hectares;
        this.bundesland = bundesland;
        this.coordinateE = coordinateE;
        this.coordinateN = coordinateN;
        this.locationDescription = locationDescription;
        this.insured = insured;
    }

    public Long getId() { return id; }
    public CropType getCropType() { return cropType; }
    public Double getHectares() { return hectares; }
    public Bundesland getBundesland() { return bundesland; }
    public Double getCoordinateE() { return coordinateE; }
    public Double getCoordinateN() { return coordinateN; }
    public String getLocationDescription() { return locationDescription; }
    public Insured getInsured() { return insured; }
}
