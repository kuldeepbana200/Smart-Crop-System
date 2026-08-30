package com.smartcrop.farmer.entity;

import com.smartcrop.auth.entity.User;
import jakarta.validation.constraints.Min;
import jakarta.persistence.*;

@Entity
@Table(name = "farmers")
public class Farmer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "district", nullable = true)
    private String district;

    @Column(name = "state", nullable = true)
    private String state;

    private Double latitude;

    private Double longitude;

    @Min(value = 0, message = "Land area must be greater than or equal to 0")
    private Double landArea;

    public Farmer() {
    }

    public Farmer(Long id, User user, String district, String state,
            Double latitude, Double longitude, Double landArea) {
        this.id = id;
        this.user = user;
        this.district = district;
        this.state = state;
        this.latitude = latitude;
        this.longitude = longitude;
        this.landArea = landArea;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLandArea() {
        return landArea;
    }

    public void setLandArea(Double landArea) {
        this.landArea = landArea;
    }
}