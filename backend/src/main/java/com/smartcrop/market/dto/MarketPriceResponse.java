package com.smartcrop.market.dto;

import java.time.LocalDate;

public class MarketPriceResponse {
    private Long id;
    private String state;
    private String district;
    private String market;
    private String commodity;
    private String variety;
    private String grade;
    private LocalDate arrivalDate;
    private Double minPrice;
    private Double maxPrice;
    private Double modalPrice;
    private String unit = "quintal"; // Default unit
    private String currency = "INR"; // Default currency

    // Constructors
    public MarketPriceResponse() {
    }

    public MarketPriceResponse(Long id, String state, String district, String market, String commodity,
            String variety, String grade, LocalDate arrivalDate,
            Double minPrice, Double maxPrice, Double modalPrice) {
        this.id = id;
        this.state = state;
        this.district = district;
        this.market = market;
        this.commodity = commodity;
        this.variety = variety;
        this.grade = grade;
        this.arrivalDate = arrivalDate;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.modalPrice = modalPrice;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getCommodity() {
        return commodity;
    }

    public void setCommodity(String commodity) {
        this.commodity = commodity;
    }

    public String getVariety() {
        return variety;
    }

    public void setVariety(String variety) {
        this.variety = variety;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public LocalDate getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(LocalDate arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public Double getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(Double minPrice) {
        this.minPrice = minPrice;
    }

    public Double getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(Double maxPrice) {
        this.maxPrice = maxPrice;
    }

    public Double getModalPrice() {
        return modalPrice;
    }

    public void setModalPrice(Double modalPrice) {
        this.modalPrice = modalPrice;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    // Methods expected by DashboardService
    public String cropName() {
        return commodity;
    }

    public String marketName() {
        return market;
    }

    public Double modalPrice() {
        return modalPrice;
    }

    public LocalDate observedAt() {
        return arrivalDate;
    }

    public String unit() {
        return unit;
    }

    public String currency() {
        return currency;
    }
}