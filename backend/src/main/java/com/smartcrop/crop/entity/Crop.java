package com.smartcrop.crop.entity;

import com.smartcrop.farmer.entity.Farmer;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "crops")
public class Crop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "farmer_id", nullable = false)
    private Farmer farmer;

    @Column(nullable = false)
    private String cropName;

    private String cropStage;

    private LocalDate sowingDate;

    private LocalDate expectedHarvestDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Crop() {
    }

    public Crop(Long id, Farmer farmer, String cropName, String cropStage,
            LocalDate sowingDate, LocalDate expectedHarvestDate, LocalDateTime createdAt) {
        this.id = id;
        this.farmer = farmer;
        this.cropName = cropName;
        this.cropStage = cropStage;
        this.sowingDate = sowingDate;
        this.expectedHarvestDate = expectedHarvestDate;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Farmer getFarmer() {
        return farmer;
    }

    public String getCropName() {
        return cropName;
    }

    public String getCropStage() {
        return cropStage;
    }

    public LocalDate getSowingDate() {
        return sowingDate;
    }

    public LocalDate getExpectedHarvestDate() {
        return expectedHarvestDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void updateDetails(String cropName, String cropStage,
            LocalDate sowingDate, LocalDate expectedHarvestDate) {
        this.cropName = cropName;
        this.cropStage = cropStage;
        this.sowingDate = sowingDate;
        this.expectedHarvestDate = expectedHarvestDate;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}