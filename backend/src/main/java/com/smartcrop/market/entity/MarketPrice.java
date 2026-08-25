package com.smartcrop.market.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "market_prices", indexes = {
        @Index(name = "idx_market_prices_crop_observed", columnList = "crop_name, observed_at"),
        @Index(name = "idx_market_prices_market_observed", columnList = "market_id, observed_at")
}, uniqueConstraints = @UniqueConstraint(name = "uk_market_price_observation", columnNames = { "market_id", "crop_name",
        "variety", "unit", "observed_at" }))
public class MarketPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "market_id", nullable = false)
    private Market market;

    @Column(name = "crop_name", nullable = false, length = 100)
    private String cropName;

    @Column(nullable = false, length = 100)
    private String variety;

    @Column(nullable = false, length = 30)
    private String unit;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal minPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal maxPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal modalPrice;

    @Column(nullable = false, length = 10)
    private String currency;

    @Column(nullable = false)
    private LocalDateTime observedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public MarketPrice() {
    }

    public MarketPrice(Long id, Market market, String cropName, String variety, String unit,
            BigDecimal minPrice, BigDecimal maxPrice, BigDecimal modalPrice, String currency,
            LocalDateTime observedAt, LocalDateTime createdAt) {
        this.id = id;
        this.market = market;
        this.cropName = cropName;
        this.variety = variety;
        this.unit = unit;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.modalPrice = modalPrice;
        this.currency = currency;
        this.observedAt = observedAt;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Market getMarket() {
        return market;
    }

    public String getCropName() {
        return cropName;
    }

    public String getVariety() {
        return variety;
    }

    public String getUnit() {
        return unit;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public BigDecimal getModalPrice() {
        return modalPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public LocalDateTime getObservedAt() {
        return observedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
