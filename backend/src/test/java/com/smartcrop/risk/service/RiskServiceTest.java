package com.smartcrop.risk.service;

import com.smartcrop.auth.entity.Role;
import com.smartcrop.auth.entity.User;
import com.smartcrop.auth.repository.UserRepository;
import com.smartcrop.crop.entity.Crop;
import com.smartcrop.crop.repository.CropRepository;
import com.smartcrop.distress.service.DistressAlertService;
import com.smartcrop.farmer.entity.Farmer;
import com.smartcrop.farmer.repository.FarmerRepository;
import com.smartcrop.risk.engine.RiskEngine;
import com.smartcrop.risk.dto.AssessRiskRequest;
import com.smartcrop.weather.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

class RiskServiceTest {

    private UserRepository userRepository;
    private FarmerRepository farmerRepository;
    private CropRepository cropRepository;
    private WeatherService weatherService;
    private RiskEngine riskEngine;
    private DistressAlertService distressAlertService;
    private RiskService riskService;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        farmerRepository = mock(FarmerRepository.class);
        cropRepository = mock(CropRepository.class);
        weatherService = mock(WeatherService.class);
        riskEngine = mock(RiskEngine.class);
        distressAlertService = mock(DistressAlertService.class);
        riskService = new RiskService(
                userRepository, farmerRepository, cropRepository, weatherService, riskEngine, distressAlertService);
        authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("farmer@example.com");
    }

    @Test
    void ownedCropSucceeds() {
        User user = user();
        Farmer farmer = farmer(user);
        Crop crop = crop(farmer);
        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(user));
        when(farmerRepository.findByUserId(10L)).thenReturn(Optional.of(farmer));
        when(cropRepository.findByIdAndFarmerId(30L, 20L)).thenReturn(Optional.of(crop));
        when(weatherService.getForecast(authentication)).thenReturn(null);
        when(riskEngine.assess(crop, null)).thenReturn(new RiskEngine.RiskResult(0, "LOW", java.util.List.of(),
                "Continue normal crop monitoring."));

        riskService.assessRisk(new AssessRiskRequest(30L), authentication);

        verify(cropRepository).findByIdAndFarmerId(30L, 20L);
        verify(weatherService).getForecast(authentication);
        verify(distressAlertService, never()).createIfRequired(any(), any(), any());
    }

    @Test
    void nonOwnedCropReturnsNotFound() {
        User user = user();
        Farmer farmer = farmer(user);
        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(user));
        when(farmerRepository.findByUserId(10L)).thenReturn(Optional.of(farmer));
        when(cropRepository.findByIdAndFarmerId(99L, 20L)).thenReturn(Optional.empty());

        assertThrows(RiskService.CropNotFoundException.class,
                () -> riskService.assessRisk(new AssessRiskRequest(99L), authentication));
    }

    @Test
    void missingFarmerProfileReturnsNotFound() {
        User user = user();
        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(user));
        when(farmerRepository.findByUserId(10L)).thenReturn(Optional.empty());

        assertThrows(RiskService.FarmerProfileNotFoundException.class,
                () -> riskService.assessRisk(new AssessRiskRequest(30L), authentication));
    }

    @Test
    void criticalRiskDelegatesAlertCreation() {
        User user = user();
        Farmer farmer = farmer(user);
        Crop crop = crop(farmer);
        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(user));
        when(farmerRepository.findByUserId(10L)).thenReturn(Optional.of(farmer));
        when(cropRepository.findByIdAndFarmerId(30L, 20L)).thenReturn(Optional.of(crop));
        when(weatherService.getForecast(authentication)).thenReturn(null);
        when(riskEngine.assess(crop, null)).thenReturn(new RiskEngine.RiskResult(90, "CRITICAL",
                java.util.List.of(new com.smartcrop.risk.dto.RiskFactor("HEAVY_RAINFALL", "HIGH", 30, "rain")),
                "Inspect drainage."));

        riskService.assessRisk(new AssessRiskRequest(30L), authentication);

        verify(distressAlertService).createIfRequired(any(), any(), any());
    }

    @Test
    void moderateRiskDoesNotCreateAlert() {
        User user = user();
        Farmer farmer = farmer(user);
        Crop crop = crop(farmer);
        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(user));
        when(farmerRepository.findByUserId(10L)).thenReturn(Optional.of(farmer));
        when(cropRepository.findByIdAndFarmerId(30L, 20L)).thenReturn(Optional.of(crop));
        when(weatherService.getForecast(authentication)).thenReturn(null);
        when(riskEngine.assess(crop, null)).thenReturn(new RiskEngine.RiskResult(30, "MODERATE",
                java.util.List.of(), "Continue monitoring."));

        riskService.assessRisk(new AssessRiskRequest(30L), authentication);

        verify(distressAlertService, never()).createIfRequired(any(), any(), any());
    }

    private User user() {
        return new User(10L, "Farmer", "farmer@example.com", null, "hash", Role.FARMER, null, null);
    }

    private Farmer farmer(User user) {
        return new Farmer(20L, user, "District", "State", 1.0, 2.0, 3.0);
    }

    private Crop crop(Farmer farmer) {
        return new Crop(30L, farmer, "Rice", "VEGETATIVE", null, null, null);
    }
}
