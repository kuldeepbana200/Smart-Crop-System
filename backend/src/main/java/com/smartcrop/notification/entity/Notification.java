package com.smartcrop.notification.entity;

import com.smartcrop.auth.entity.User;
import com.smartcrop.distress.entity.DistressAlert;
import com.smartcrop.intervention.entity.Intervention;
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
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "distress_alert_id")
    private DistressAlert distressAlert;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intervention_id")
    private Intervention intervention;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime readAt;

    public Notification() {
    }

    public Notification(Long id, User recipient, DistressAlert distressAlert,
            Intervention intervention, NotificationType type, String title, String message,
            NotificationStatus status, LocalDateTime createdAt, LocalDateTime readAt) {
        this.id = id;
        this.recipient = recipient;
        this.distressAlert = distressAlert;
        this.intervention = intervention;
        this.type = type;
        this.title = title;
        this.message = message;
        this.status = status;
        this.createdAt = createdAt;
        this.readAt = readAt;
    }

    public Long getId() {
        return id;
    }

    public User getRecipient() {
        return recipient;
    }

    public DistressAlert getDistressAlert() {
        return distressAlert;
    }

    public Intervention getIntervention() {
        return intervention;
    }

    public NotificationType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void markAsRead(LocalDateTime readAt) {
        if (status == NotificationStatus.UNREAD) {
            status = NotificationStatus.READ;
            this.readAt = readAt;
        }
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = NotificationStatus.UNREAD;
        }
    }
}
