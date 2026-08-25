package com.smartcrop.advisory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "advisory_recommendations")
public class AdvisoryRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "advisory_id", nullable = false)
    private Advisory advisory;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String severity;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String recommendation;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    public AdvisoryRecommendation() {
    }

    public AdvisoryRecommendation(Long id, Advisory advisory, String category, String severity,
            String title, String recommendation, String reason) {
        this.id = id;
        this.advisory = advisory;
        this.category = category;
        this.severity = severity;
        this.title = title;
        this.recommendation = recommendation;
        this.reason = reason;
    }

    public Long getId() {
        return id;
    }

    public Advisory getAdvisory() {
        return advisory;
    }

    public void setAdvisory(Advisory advisory) {
        this.advisory = advisory;
    }

    public String getCategory() {
        return category;
    }

    public String getSeverity() {
        return severity;
    }

    public String getTitle() {
        return title;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public String getReason() {
        return reason;
    }
}
