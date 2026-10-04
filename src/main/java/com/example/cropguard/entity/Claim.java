package com.example.cropguard.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/** A hail damage claim filed against a policy. Module 5: JPA entity. */
@Entity
@Table(name = "claims")
public class Claim {

    public enum Status { SUBMITTED, UNDER_REVIEW, ASSESSED, APPROVED, REJECTED, PAID }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate damageDate;

    @Column(nullable = false, length = 3000)
    private String damageDescription;

    /** Assessed damage as percentage of the plot (0–100). Set by assessor. */
    @Column
    private Double damagePercent;

    /** Calculated payout in EUR. Set when claim is approved. */
    @Column
    private Double payoutEur;

    /** Free-text notes from the assessor. */
    @Column(length = 3000)
    private String assessorNotes;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    /** Link to the hail event that caused this claim, if known. */
    @Column
    private Long hailEventId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    /** Assessor who reviewed this claim. */
    @Column
    private String assessedBy;

    protected Claim() {}

    public Claim(LocalDate damageDate, String damageDescription, Status status, Policy policy) {
        this.damageDate = damageDate;
        this.damageDescription = damageDescription;
        this.status = status;
        this.policy = policy;
    }

    public Long getId() { return id; }
    public LocalDate getDamageDate() { return damageDate; }
    public String getDamageDescription() { return damageDescription; }
    public Double getDamagePercent() { return damagePercent; }
    public Double getPayoutEur() { return payoutEur; }
    public String getAssessorNotes() { return assessorNotes; }
    public Status getStatus() { return status; }
    public Long getHailEventId() { return hailEventId; }
    public Policy getPolicy() { return policy; }
    public String getAssessedBy() { return assessedBy; }

    public void setDamagePercent(Double damagePercent) { this.damagePercent = damagePercent; }
    public void setPayoutEur(Double payoutEur) { this.payoutEur = payoutEur; }
    public void setAssessorNotes(String assessorNotes) { this.assessorNotes = assessorNotes; }
    public void setStatus(Status status) { this.status = status; }
    public void setHailEventId(Long hailEventId) { this.hailEventId = hailEventId; }
    public void setAssessedBy(String assessedBy) { this.assessedBy = assessedBy; }
}
