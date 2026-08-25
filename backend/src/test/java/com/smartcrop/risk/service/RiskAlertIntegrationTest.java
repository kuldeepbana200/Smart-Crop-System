package com.smartcrop.risk.service;

import com.smartcrop.auth.entity.Role;
import com.smartcrop.auth.entity.User;
import com.smartcrop.auth.repository.UserRepository;
import com.smartcrop.crop.entity.Crop;
import com.smartcrop.crop.repository.CropRepository;
import com.smartcrop.distress.entity.AlertStatus;
import com.smartcrop.distress.entity.DistressAlert;
import com.smartcrop.distress.repository.DistressAlertRepository;
import com.smartcrop.distress.service.DistressAlertService;
import com.smartcrop.farmer.entity.Farmer;
import com.smartcrop.farmer.repository.FarmerRepository;
import com.smartcrop.risk.dto.AssessRiskRequest;
import com.smartcrop.risk.dto.RiskAssessmentResponse;
import com.smartcrop.risk.engine.RiskEngine;
import com.smartcrop.weather.dto.CurrentWeatherResponse;
import com.smartcrop.weather.dto.WeatherForecastResponse;
import com.smartcrop.weather.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RiskAlertIntegrationTest {

        private UserRepository userRepository;
        private FarmerRepository farmerRepository;
        private CropRepository cropRepository;
        private WeatherService weatherService;
        private DistressAlertRepository alertRepository;
        private RiskService riskService;
        private Authentication authentication;
        private Farmer farmer;
        private Crop crop;

        @BeforeEach
        void setUp() {
                userRepository = mock(UserRepository.class);
                farmerRepository = mock(FarmerRepository.class);
                cropRepository = mock(CropRepository.class);
                weatherService = mock(WeatherService.class);
                alertRepository = mock(DistressAlertRepository.class);
                DistressAlertService alertService = new DistressAlertService(
                                alertRepository, userRepository, farmerRepository);
                riskService = new RiskService(
                                userRepository,
                                farmerRepository,
                                cropRepository,
                                weatherService,
                                new RiskEngine(),
                                alertService);

                User user = new User(10L, "Farmer", "farmer@example.com", null, "hash", Role.FARMER, null, null);
                farmer = new Farmer(20L, user, "District", "State", 1.0, 2.0, 3.0);
                crop = new Crop(30L, farmer, "Rice", "VEGETATIVE", null, null, null);
                authentication = mock(Authentication.class);
                when(authentication.getName()).thenReturn("farmer@example.com");
                when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(user));
                when(farmerRepository.findByUserId(10L)).thenReturn(Optional.of(farmer));
                when(cropRepository.findByIdAndFarmerId(30L, 20L)).thenReturn(Optional.of(crop));
                when(alertRepository.findByFarmerIdAndCropIdAndConditionKeyAndStatus(
                                20L, 30L, "EXTREME_HEAT|HEAVY_RAINFALL", AlertStatus.OPEN))
                                .thenReturn(Optional.empty());
                when(alertRepository.findByFarmerIdAndCropIdAndConditionKeyAndStatus(
                                20L, 30L, "EXTREME_HEAT|HEAVY_RAINFALL", AlertStatus.ACKNOWLEDGED))
                                .thenReturn(Optional.empty());
                when(alertRepository.save(any(DistressAlert.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));
        }

        @Test
        void highSyntheticRiskCreatesOpenAlert() {
                when(weatherService.getForecast(authentication)).thenReturn(weather(
                                "VEGETATIVE", 80, 12, 39, 15, 10, 3));

                RiskAssessmentResponse result = riskService.assessRisk(
                                new AssessRiskRequest(30L), authentication);

                assertEquals(55, result.riskScore());
                assertEquals("HIGH", result.riskLevel());
                assertEquals(2, result.factors().size());
                assertTrue(result.factors().stream().anyMatch(factor -> "HEAVY_RAINFALL".equals(factor.type())));
                assertTrue(result.factors().stream().anyMatch(factor -> "EXTREME_HEAT".equals(factor.type())));

                ArgumentCaptor<DistressAlert> captor = ArgumentCaptor.forClass(DistressAlert.class);
                verify(alertRepository).save(captor.capture());
                assertEquals(AlertStatus.OPEN, captor.getValue().getStatus());
                assertEquals("HIGH", captor.getValue().getRiskLevel());
                assertEquals(55, captor.getValue().getRiskScore());
        }

        @Test
        void criticalSyntheticRiskCreatesOneCriticalAlert() {
                crop = new Crop(30L, farmer, "Rice", "FLOWERING", null, null, null);
                when(cropRepository.findByIdAndFarmerId(30L, 20L)).thenReturn(Optional.of(crop));
                when(weatherService.getForecast(authentication)).thenReturn(weather(
                                "FLOWERING", 80, 12, 40, 5, 40, 6));

                RiskAssessmentResponse result = riskService.assessRisk(
                                new AssessRiskRequest(30L), authentication);

                assertEquals(100, result.riskScore());
                assertEquals("CRITICAL", result.riskLevel());
                assertEquals(5, result.factors().size());
                assertTrue(result.factors().stream().anyMatch(factor -> "HEAVY_RAINFALL".equals(factor.type())));
                assertTrue(result.factors().stream().anyMatch(factor -> "EXTREME_HEAT".equals(factor.type())));
                assertTrue(result.factors().stream().anyMatch(factor -> "COLD_STRESS".equals(factor.type())));
                assertTrue(result.factors().stream().anyMatch(factor -> "STRONG_WIND".equals(factor.type())));
                assertTrue(result.factors().stream()
                                .anyMatch(factor -> "HIGH_EVAPOTRANSPIRATION".equals(factor.type())));

                ArgumentCaptor<DistressAlert> captor = ArgumentCaptor.forClass(DistressAlert.class);
                verify(alertRepository).save(captor.capture());
                assertEquals("CRITICAL", captor.getValue().getRiskLevel());
                assertEquals(100, captor.getValue().getRiskScore());
        }

        @Test
        void moderateFloweringHeavyRainfallDoesNotCreateAlert() {
                crop = new Crop(30L, farmer, "Rice", "FLOWERING", null, null, null);
                when(cropRepository.findByIdAndFarmerId(30L, 20L)).thenReturn(Optional.of(crop));
                when(weatherService.getForecast(authentication)).thenReturn(weather(
                                "FLOWERING", 100, 51.6, 25, 15, 10, 3));

                RiskAssessmentResponse result = riskService.assessRisk(
                                new AssessRiskRequest(30L), authentication);

                assertEquals(38, result.riskScore());
                assertEquals("MODERATE", result.riskLevel());
                verify(alertRepository, never()).save(any(DistressAlert.class));
        }

        @Test
        void lowSyntheticRiskDoesNotCreateAlert() {
                when(weatherService.getForecast(authentication)).thenReturn(weather(
                                "VEGETATIVE", 20, 1, 25, 15, 10, 3));

                RiskAssessmentResponse result = riskService.assessRisk(
                                new AssessRiskRequest(30L), authentication);

                assertEquals(0, result.riskScore());
                assertEquals("LOW", result.riskLevel());
                verify(alertRepository, never()).save(any(DistressAlert.class));
        }

        @Test
        void identicalHighAssessmentCreatesOnlyOneAlert() {
                crop = new Crop(30L, farmer, "Rice", "VEGETATIVE", null, null, null);
                WeatherForecastResponse weather = weather("VEGETATIVE", 80, 12, 39, 15, 10, 3);
                when(weatherService.getForecast(authentication)).thenReturn(weather);
                when(alertRepository.findByFarmerIdAndCropIdAndConditionKeyAndStatus(
                                20L, 30L, "EXTREME_HEAT|HEAVY_RAINFALL", AlertStatus.OPEN))
                                .thenReturn(Optional.empty(), Optional.of(new DistressAlert()));

                riskService.assessRisk(new AssessRiskRequest(30L), authentication);
                riskService.assessRisk(new AssessRiskRequest(30L), authentication);

                verify(alertRepository).save(any(DistressAlert.class));
        }

        @Test
        void resolvedConditionAllowsAnotherAlert() {
                crop = new Crop(30L, farmer, "Rice", "VEGETATIVE", null, null, null);
                when(weatherService.getForecast(authentication)).thenReturn(
                                weather("VEGETATIVE", 80, 12, 39, 15, 10, 3));

                riskService.assessRisk(new AssessRiskRequest(30L), authentication);
                riskService.assessRisk(new AssessRiskRequest(30L), authentication);

                verify(alertRepository, org.mockito.Mockito.times(2)).save(any(DistressAlert.class));
        }

        private WeatherForecastResponse weather(
                        String stage,
                        double precipitationProbability,
                        double precipitation,
                        double temperatureMax,
                        double temperatureMin,
                        double windSpeedMax,
                        double evapotranspiration) {
                CurrentWeatherResponse current = new CurrentWeatherResponse(
                                "2026-08-23T12:00", "Asia/Kolkata", 30.0, 70.0, 0.0, 10.0, 1);
                WeatherForecastResponse.HourlyForecast hourly = new WeatherForecastResponse.HourlyForecast(
                                List.of("2026-08-23T12:00"), List.of(30.0), List.of(70.0),
                                List.of(precipitationProbability), List.of(precipitation), List.of(windSpeedMax),
                                List.of(1));
                WeatherForecastResponse.DailyForecast daily = new WeatherForecastResponse.DailyForecast(
                                List.of("2026-08-23"), List.of(1), List.of(temperatureMax), List.of(temperatureMin),
                                List.of(precipitation), List.of(precipitationProbability), List.of(windSpeedMax),
                                List.of(evapotranspiration));
                return new WeatherForecastResponse("Asia/Kolkata", current, hourly, daily);
        }
}
