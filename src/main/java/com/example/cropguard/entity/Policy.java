package com.example.cropguard.entity;

import com.example.cropguard.domain.Deductible;
import jakarta.persistence.*;
import java.time.LocalDate;

/** An insurance policy linking a plot to coverage. Module 5: JPA entity. */
@Entity
@Table(name = "policies")
public class Policy {

    public enum Status { QUOTE, ACTIVE, EXPIRED, CANCELLED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double coverageEur;

    @Column(nullable = false)
    private Double premiumEur;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Deductible deductible;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    /** Coverage window — typically the growing season. */
    @Column(nullable = false)
    private LocalDate coverageStart;

    @Column(nullable = false)
    private LocalDate coverageEnd;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plot_id", nullable = false)
    private Plot plot;

    protected Policy() {}

    public Policy(Double coverageEur, Double premiumEur, Deductible deductible,
                  Status status, LocalDate coverageStart, LocalDate coverageEnd, Plot plot) {
        this.coverageEur = coverageEur;
        this.premiumEur = premiumEur;
        this.deductible = deductible;
        this.status = status;
        this.coverageStart = coverageStart;
        this.coverageEnd = coverageEnd;
        this.plot = plot;
    }

    public Long getId() { return id; }
    public Double getCoverageEur() { return coverageEur; }
    public Double getPremiumEur() { return premiumEur; }
    public Deductible getDeductible() { return deductible; }
    public Status getStatus() { return status; }
    public LocalDate getCoverageStart() { return coverageStart; }
    public LocalDate getCoverageEnd() { return coverageEnd; }
    public Plot getPlot() { return plot; }

    public void setStatus(Status status) { this.status = status; }
}
