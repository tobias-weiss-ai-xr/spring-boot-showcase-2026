package com.example.cropguard.entity;

import com.example.cropguard.domain.Bundesland;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

/** A hailstorm event affecting one or more Bundesländer. Module 5: JPA entity. */
@Entity
@Table(name = "hail_events")
public class HailEvent {

    public enum Severity { LIGHT, MODERATE, SEVERE, DEVASTATING }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate eventDate;

    /** Affected Bundesländer as comma-separated enum names (e.g. "BAYERN,HESSEN"). */
    @Column(nullable = false)
    private String affectedBundeslaender;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Severity severity;

    @Column(length = 2000)
    private String description;

    /** Estimated hailstone diameter in mm, if known. */
    @Column
    private Integer hailstoneDiameterMm;

    protected HailEvent() {}

    public HailEvent(LocalDate eventDate, String affectedBundeslaender, Severity severity,
                     String description, Integer hailstoneDiameterMm) {
        this.eventDate = eventDate;
        this.affectedBundeslaender = affectedBundeslaender;
        this.severity = severity;
        this.description = description;
        this.hailstoneDiameterMm = hailstoneDiameterMm;
    }

    public Long getId() { return id; }
    public LocalDate getEventDate() { return eventDate; }
    public String getAffectedBundeslaender() { return affectedBundeslaender; }
    public Severity getSeverity() { return severity; }
    public String getDescription() { return description; }
    public Integer getHailstoneDiameterMm() { return hailstoneDiameterMm; }

    /** Parse the comma-separated Bundesland string into a list. */
    public List<Bundesland> getAffectedBundeslandList() {
        if (affectedBundeslaender == null || affectedBundeslaender.isBlank()) return List.of();
        return java.util.Arrays.stream(affectedBundeslaender.split(","))
            .map(String::trim)
            .map(Bundesland::valueOf)
            .toList();
    }
}
