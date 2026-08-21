package com.smartcrop.farmer.entity;

import com.smartcrop.auth.entity.User;
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

    @Column(nullable = false)
    private String district;

    @Column(nullable = false)
    private String state;

    private Double latitude;

    private Double longitude;

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

    public String getDistrict() {
        return district;
    }

    public String getState() {
        return state;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getLandArea() {
        return landArea;
    }
}