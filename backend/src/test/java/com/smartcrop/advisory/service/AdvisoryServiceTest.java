package com.smartcrop.advisory.service;

import com.smartcrop.advisory.dto.AdvisoryRecommendation;
import com.smartcrop.advisory.dto.GenerateAdvisoryRequest;
import com.smartcrop.advisory.repository.AdvisoryRepository;
import com.smartcrop.auth.entity.Role;
import com.smartcrop.auth.entity.User;
import com.smartcrop.auth.repository.UserRepository;
import com.smartcrop.crop.entity.Crop;
import com.smartcrop.crop.repository.CropRepository;
import com.smartcrop.farmer.entity.Farmer;
import com.smartcrop.farmer.repository.FarmerRepository;
import com.smartcrop.notification.service.NotificationService;
import com.smartcrop.risk.engine.RiskEngine;
import com.smartcrop.weather.dto.WeatherForecastResponse;
import com.smartcrop.weather.service.WeatherService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdvisoryServiceTest {

        private UserRepository userRepository;
        private FarmerRepository farmerRepository;
        private CropRepository cropRepository;
        private WeatherService weatherService;
        private RiskEngine riskEngine;
        private GroqAdvisoryService groqAdvisoryService;
        private NotificationService notificationService;
        private AdvisoryRepository advisoryRepository;
        private AdvisoryService advisoryService;
        private Authentication authentication;

        @BeforeEach
        void setUp() {
                userRepository = mock(UserRepository.class);
                farmerRepository = mock(FarmerRepository.class);
                cropRepository = mock(CropRepository.class);
                weatherService = mock(WeatherService.class);
                riskEngine = mock(RiskEngine.class);
                groqAdvisoryService = mock(GroqAdvisoryService.class);
                notificationService = mock(NotificationService.class);
                advisoryRepository = mock(AdvisoryRepository.class);

                advisoryService = new AdvisoryService(
                                userRepository,
                                farmerRepository,
                                cropRepository,
                                weatherService,
                                riskEngine,
                                groqAdvisoryService,
                                notificationService,
                                advisoryRepository);

                authentication = mock(Authentication.class);

                when(authentication.getName())
                                .thenReturn("farmer@example.com");
        }

        @Test
        void generatesAdvisoryOnlyForAuthenticatedFarmersCrop() {

                User user = new User(
                                10L,
                                "Farmer",
                                "farmer@example.com",
                                null,
                                "hash",
                                Role.FARMER,
                                null,
                                null);

                Farmer farmer = new Farmer(
                                20L,
                                user,
                                "District",
                                "State",
                                1.0,
                                2.0,
                                3.0);

                Crop crop = new Crop(
                                30L,
                                farmer,
                                "Rice",
                                "FLOWERING",
                                null,
                                null,
                                null);

                when(userRepository.findByEmail("farmer@example.com"))
                                .thenReturn(Optional.of(user));

                when(farmerRepository.findByUserId(10L))
                                .thenReturn(Optional.of(farmer));

                when(cropRepository.findByIdAndFarmerId(30L, 20L))
                                .thenReturn(Optional.of(crop));

                WeatherForecastResponse weather = createValidWeather();

                when(weatherService.getForecast(authentication))
                                .thenReturn(weather);

                when(groqAdvisoryService.generateForFarmer(any(), any(), any(), any(), any()))
                                .thenReturn(List.of(
                                                new AdvisoryRecommendation(
                                                                "RAINFALL",
                                                                "WARNING",
                                                                "Heavy rain",
                                                                "Delay irrigation.",
                                                                "Rain is expected.")));

                when(advisoryRepository.save(any()))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                advisoryService.generateAdvisory(
                                new GenerateAdvisoryRequest(30L, "en"),
                                authentication);

                verify(cropRepository)
                                .findByIdAndFarmerId(30L, 20L);

                verify(weatherService)
                                .getForecast(authentication);

                verify(groqAdvisoryService)
                                .generateForFarmer(any(), any(), any(), any(), any());

                verify(advisoryRepository)
                                .save(any());
        }

        @Test
        void generatesGroqAdvisoryAndCreatesHighPriorityNotification() {

                User user = new User(
                                10L,
                                "Farmer",
                                "farmer@example.com",
                                null,
                                "hash",
                                Role.FARMER,
                                null,
                                null);

                Farmer farmer = new Farmer(
                                20L,
                                user,
                                "District",
                                "State",
                                1.0,
                                2.0,
                                3.0);

                Crop crop = new Crop(
                                30L,
                                farmer,
                                "Rice",
                                "FLOWERING",
                                null,
                                null,
                                null);

                when(userRepository.findByEmail("farmer@example.com"))
                                .thenReturn(Optional.of(user));
                when(farmerRepository.findByUserId(10L))
                                .thenReturn(Optional.of(farmer));
                when(cropRepository.findByIdAndFarmerId(30L, 20L))
                                .thenReturn(Optional.of(crop));

                WeatherForecastResponse weather = createValidWeather();
                when(weatherService.getForecast(authentication))
                                .thenReturn(weather);
                when(riskEngine.assess(crop, weather))
                                .thenReturn(new RiskEngine.RiskResult(80, "HIGH", List.of(), "Inspect crop."));
                when(groqAdvisoryService.generateForFarmer(farmer, crop, weather, riskEngine.assess(crop, weather),
                                "en"))
                                .thenReturn(List.of(
                                                new AdvisoryRecommendation(
                                                                "TEMPERATURE",
                                                                "URGENT",
                                                                "Extreme heat expected",
                                                                "Irrigate during cooler hours.",
                                                                "Heat stress risk is high.")));
                when(advisoryRepository.save(any()))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                advisoryService.generateAdvisory(new GenerateAdvisoryRequest(30L, "en"), authentication);

                verify(notificationService).notifyAdvisoryGenerated(eq(user), eq(crop), any());
        }

        @Test
        void rejectsMissingFarmerProfileBeforeLookingUpCrop() {

                User user = new User(
                                10L,
                                "Farmer",
                                "farmer@example.com",
                                null,
                                "hash",
                                Role.FARMER,
                                null,
                                null);

                when(userRepository.findByEmail("farmer@example.com"))
                                .thenReturn(Optional.of(user));

                when(farmerRepository.findByUserId(10L))
                                .thenReturn(Optional.empty());

                assertThrows(
                                AdvisoryService.FarmerProfileNotFoundException.class,
                                () -> advisoryService.generateAdvisory(
                                                new GenerateAdvisoryRequest(30L, "en"),
                                                authentication));
        }

        @Test
        void treatsNonOwnedCropAsNotFound() {

                User user = new User(
                                10L,
                                "Farmer",
                                "farmer@example.com",
                                null,
                                "hash",
                                Role.FARMER,
                                null,
                                null);

                Farmer farmer = new Farmer(
                                20L,
                                user,
                                "District",
                                "State",
                                1.0,
                                2.0,
                                3.0);

                when(userRepository.findByEmail("farmer@example.com"))
                                .thenReturn(Optional.of(user));

                when(farmerRepository.findByUserId(10L))
                                .thenReturn(Optional.of(farmer));

                when(cropRepository.findByIdAndFarmerId(99L, 20L))
                                .thenReturn(Optional.empty());

                AdvisoryService.CropNotFoundException exception = assertThrows(
                                AdvisoryService.CropNotFoundException.class,
                                () -> advisoryService.generateAdvisory(
                                                new GenerateAdvisoryRequest(99L, "en"),
                                                authentication));

                assertEquals(
                                AdvisoryService.CropNotFoundException.class,
                                exception.getClass());
        }

        private WeatherForecastResponse createValidWeather() {

                WeatherForecastResponse.DailyForecast daily = new WeatherForecastResponse.DailyForecast(
                                List.of("2026-08-27"),
                                List.of(1),
                                List.of(30.0),
                                List.of(20.0),
                                List.of(2.0),
                                List.of(20.0),
                                List.of(15.0),
                                List.of(3.0));

                return new WeatherForecastResponse(
                                "Asia/Kolkata",
                                null,
                                null,
                                daily);
        }
}