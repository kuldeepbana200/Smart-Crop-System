package com.smartcrop.advisory.entity;

import com.smartcrop.crop.entity.Crop;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "advisories")
public class Advisory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "crop_id", nullable = false)
    private Crop crop;

    @Column(nullable = false, updatable = false)
    private LocalDateTime generatedAt;

    @Column(nullable = false, length = 2)
    private String language;

    @OneToMany(mappedBy = "advisory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AdvisoryRecommendation> recommendations = new ArrayList<>();

    // Required by JPA
    public Advisory() {
    }

    public Advisory(
            Long id,
            Crop crop,
            LocalDateTime generatedAt,
            List<AdvisoryRecommendation> recommendations,
            String language) {
        this.id = id;
        this.crop = crop;
        this.generatedAt = generatedAt;
        this.recommendations = recommendations == null
                ? new ArrayList<>()
                : recommendations;
        this.language = language;
    }

    public Long getId() {
        return id;
    }

    public Crop getCrop() {
        return crop;
    }

    public void setCrop(Crop crop) {
        this.crop = crop;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public List<AdvisoryRecommendation> getRecommendations() {
        return recommendations;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void addRecommendation(AdvisoryRecommendation recommendation) {
        recommendations.add(recommendation);
        recommendation.setAdvisory(this);
    }

    @PrePersist
    protected void onCreate() {
        if (generatedAt == null) {
            generatedAt = LocalDateTime.now();
        }
    }
}