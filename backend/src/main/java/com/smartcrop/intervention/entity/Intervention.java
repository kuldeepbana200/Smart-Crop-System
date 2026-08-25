package com.smartcrop.intervention.entity;

import com.smartcrop.auth.entity.User;
import com.smartcrop.distress.entity.DistressAlert;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "interventions")
public class Intervention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "distress_alert_id", nullable = false)
    private DistressAlert distressAlert;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "officer_id", nullable = false)
    private User officer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterventionType type;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterventionStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime completedAt;

    public Intervention() {
    }

    public Intervention(Long id, DistressAlert distressAlert, User officer,
            InterventionType type, String description, InterventionStatus status,
            LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime completedAt) {
        this.id = id;
        this.distressAlert = distressAlert;
        this.officer = officer;
        this.type = type;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.completedAt = completedAt;
    }

    public Long getId() {
        return id;
    }

    public DistressAlert getDistressAlert() {
        return distressAlert;
    }

    public User getOfficer() {
        return officer;
    }

    public InterventionType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public InterventionStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void update(InterventionStatus newStatus, String newDescription, LocalDateTime completedAt) {
        if (!isAllowedTransition(status, newStatus)) {
            throw new InvalidInterventionTransitionException();
        }
        this.status = newStatus;
        if (newDescription != null) {
            this.description = newDescription;
        }
        if (newStatus == InterventionStatus.COMPLETED) {
            this.completedAt = completedAt;
        }
    }

    private boolean isAllowedTransition(InterventionStatus current, InterventionStatus next) {
        return (current == InterventionStatus.PLANNED && (next == InterventionStatus.IN_PROGRESS
                || next == InterventionStatus.CANCELLED))
                || (current == InterventionStatus.IN_PROGRESS && (next == InterventionStatus.COMPLETED
                        || next == InterventionStatus.CANCELLED));
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public static class InvalidInterventionTransitionException extends RuntimeException {
    }
}
