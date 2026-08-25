package com.smartcrop.risk.monitoring;

import com.smartcrop.auth.entity.Role;
import com.smartcrop.auth.entity.User;
import com.smartcrop.crop.entity.Crop;
import com.smartcrop.crop.repository.CropRepository;
import com.smartcrop.distress.service.DistressAlertService;
import com.smartcrop.farmer.entity.Farmer;
import com.smartcrop.farmer.repository.FarmerRepository;
import com.smartcrop.risk.engine.RiskEngine;
import com.smartcrop.weather.dto.CurrentWeatherResponse;
import com.smartcrop.weather.dto.WeatherForecastResponse;
import com.smartcrop.weather.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RiskMonitoringSchedulerTest {

    private FarmerRepository farmerRepository;
    private CropRepository cropRepository;
    private WeatherService weatherService;
    private RiskEngine riskEngine;
    private DistressAlertService distressAlertService;
    private RiskMonitoringScheduler scheduler;
    private Farmer farmer;
    private Crop crop;

    @BeforeEach
    void setUp() {
        farmerRepository = mock(FarmerRepository.class);
        cropRepository = mock(CropRepository.class);
        weatherService = mock(WeatherService.class);
        riskEngine = mock(RiskEngine.class);
        distressAlertService = mock(DistressAlertService.class);
        scheduler = new RiskMonitoringScheduler(
                farmerRepository, cropRepository, weatherService, riskEngine, distressAlertService);

        User user = new User(10L, "Farmer", "farmer@example.com", null, "hash", Role.FARMER, null, null);
        farmer = new Farmer(20L, user, "Pune", "Maharashtra", 18.5, 73.8, 2.0);
        crop = new Crop(30L, farmer, "Rice", "FLOWERING",
                LocalDate.now().minusDays(10), LocalDate.now().plusDays(20), null);
        when(farmerRepository.findAll()).thenReturn(List.of(farmer));
        when(cropRepository.findByFarmerId(20L)).thenReturn(List.of(crop));
        when(weatherService.getForecast(any(Authentication.class))).thenReturn(weather());
    }

    @Test
    void highRiskCreatesAlertThroughExistingService() {
        when(riskEngine.assess(crop, weather())).thenReturn(result(60, "HIGH"));

        scheduler.runOnce();

        verify(distressAlertService).createIfRequired(eq(farmer), eq(crop), any());
    }

    @Test
    void criticalRiskCreatesAlertThroughExistingService() {
        when(riskEngine.assess(crop, weather())).thenReturn(result(90, "CRITICAL"));

        scheduler.runOnce();

        verify(distressAlertService).createIfRequired(eq(farmer), eq(crop), any());
    }

    @Test
    void lowAndModerateRiskDoNotCreateAlerts() {
        when(riskEngine.assess(crop, weather()))
                .thenReturn(result(10, "LOW"), result(35, "MODERATE"));

        scheduler.runOnce();
        scheduler.runOnce();

        verify(distressAlertService, never()).createIfRequired(any(), any(), any());
    }

    @Test
    void repeatedExecutionDelegatesToExistingIdempotentAlertService() {
        when(riskEngine.assess(crop, weather())).thenReturn(result(60, "HIGH"));

        scheduler.runOnce();
        scheduler.runOnce();

        verify(distressAlertService, times(2)).createIfRequired(eq(farmer), eq(crop), any());
    }

    @Test
    void weatherFailureForOneFarmerDoesNotStopOtherFarmers() {
        User otherUser = new User(11L, "Other", "other@example.com", null, "hash", Role.FARMER, null, null);
        Farmer otherFarmer = new Farmer(21L, otherUser, "Nashik", "Maharashtra", 19.9, 73.8, 2.0);
        Crop otherCrop = new Crop(31L, otherFarmer, "Wheat", "VEGETATIVE",
                LocalDate.now().minusDays(10), LocalDate.now().plusDays(20), null);
        when(farmerRepository.findAll()).thenReturn(List.of(farmer, otherFarmer));
        when(cropRepository.findByFarmerId(20L)).thenReturn(List.of(crop));
        when(cropRepository.findByFarmerId(21L)).thenReturn(List.of(otherCrop));
        when(weatherService.getForecast(any(Authentication.class)))
                .thenThrow(new RuntimeException("provider unavailable"))
                .thenReturn(weather());
        when(riskEngine.assess(otherCrop, weather())).thenReturn(result(60, "HIGH"));

        scheduler.runOnce();

        verify(distressAlertService).createIfRequired(eq(otherFarmer), eq(otherCrop), any());
    }

    @Test
    void riskFailureForOneCropDoesNotStopOtherCrops() {
        Crop otherCrop = new Crop(31L, farmer, "Wheat", "VEGETATIVE",
                LocalDate.now().minusDays(10), LocalDate.now().plusDays(20), null);
        when(cropRepository.findByFarmerId(20L)).thenReturn(List.of(crop, otherCrop));
        when(riskEngine.assess(crop, weather())).thenThrow(new RuntimeException("bad crop data"));
        when(riskEngine.assess(otherCrop, weather())).thenReturn(result(60, "HIGH"));

        scheduler.runOnce();

        verify(distressAlertService).createIfRequired(eq(farmer), eq(otherCrop), any());
    }

    @Test
    void schedulerIsConfigurableThroughConditionalProperty() {
        ConditionalOnProperty condition = RiskMonitoringScheduler.class
                .getAnnotation(ConditionalOnProperty.class);

        assertNotNull(condition);
        assertEquals("app.risk-monitoring", condition.prefix());
        assertArrayEquals(new String[] { "enabled" }, condition.name());
        assertEquals("true", condition.havingValue());
    }

    private RiskEngine.RiskResult result(int score, String level) {
        return new RiskEngine.RiskResult(score, level, List.of(), "Monitor crop.");
    }

    private WeatherForecastResponse weather() {
        CurrentWeatherResponse current = new CurrentWeatherResponse(
                "2026-08-23T12:00", "Asia/Kolkata", 30.0, 70.0, 0.0, 10.0, 1);
        WeatherForecastResponse.HourlyForecast hourly = new WeatherForecastResponse.HourlyForecast(
                List.of("2026-08-23T12:00"), List.of(30.0), List.of(70.0), List.of(80.0),
                List.of(12.0), List.of(10.0), List.of(1));
        WeatherForecastResponse.DailyForecast daily = new WeatherForecastResponse.DailyForecast(
                List.of("2026-08-23"), List.of(1), List.of(39.0), List.of(15.0),
                List.of(12.0), List.of(80.0), List.of(10.0), List.of(3.0));
        return new WeatherForecastResponse("Asia/Kolkata", current, hourly, daily);
    }
}
