package com.smartcrop.distress.entity;

import com.smartcrop.auth.entity.User;
import com.smartcrop.crop.entity.Crop;
import com.smartcrop.farmer.entity.Farmer;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "distress_alerts")
public class DistressAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "farmer_id", nullable = false)
    private Farmer farmer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "crop_id", nullable = false)
    private Crop crop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_officer_id")
    private User assignedOfficer;

    @Column(nullable = false)
    private Integer riskScore;

    @Column(nullable = false)
    private String riskLevel;

    @Column(nullable = false)
    private String conditionKey;

    @Column(nullable = false)
    private String dominantFactor;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String factorSummary;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String recommendedAction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertStatus status;

    @Column(columnDefinition = "TEXT")
    private String officerNote;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime acknowledgedAt;
    private LocalDateTime resolvedAt;

    public DistressAlert() {
    }

    public DistressAlert(Long id, Farmer farmer, Crop crop, User assignedOfficer,
            Integer riskScore, String riskLevel, String conditionKey, String dominantFactor,
            String factorSummary, String recommendedAction, AlertStatus status, String officerNote,
            LocalDateTime createdAt, LocalDateTime acknowledgedAt, LocalDateTime resolvedAt) {
        this.id = id;
        this.farmer = farmer;
        this.crop = crop;
        this.assignedOfficer = assignedOfficer;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.conditionKey = conditionKey;
        this.dominantFactor = dominantFactor;
        this.factorSummary = factorSummary;
        this.recommendedAction = recommendedAction;
        this.status = status;
        this.officerNote = officerNote;
        this.createdAt = createdAt;
        this.acknowledgedAt = acknowledgedAt;
        this.resolvedAt = resolvedAt;
    }

    public Long getId() {
        return id;
    }

    public Farmer getFarmer() {
        return farmer;
    }

    public Crop getCrop() {
        return crop;
    }

    public User getAssignedOfficer() {
        return assignedOfficer;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public String getConditionKey() {
        return conditionKey;
    }

    public String getDominantFactor() {
        return dominantFactor;
    }

    public String getFactorSummary() {
        return factorSummary;
    }

    public String getRecommendedAction() {
        return recommendedAction;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public String getOfficerNote() {
        return officerNote;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void acknowledge(String officerNote, LocalDateTime acknowledgedAt) {
        if (status != AlertStatus.OPEN) {
            throw new InvalidAlertTransitionException();
        }
        this.status = AlertStatus.ACKNOWLEDGED;
        this.officerNote = officerNote;
        this.acknowledgedAt = acknowledgedAt;
    }

    public void resolve(String officerNote, LocalDateTime resolvedAt) {
        if (status != AlertStatus.OPEN && status != AlertStatus.ACKNOWLEDGED) {
            throw new InvalidAlertTransitionException();
        }
        this.status = AlertStatus.RESOLVED;
        this.officerNote = officerNote;
        this.resolvedAt = resolvedAt;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public static class InvalidAlertTransitionException extends RuntimeException {
    }
}
