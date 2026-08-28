package com.smartcrop.advisory.service;

import com.smartcrop.advisory.dto.AdvisoryRecommendation;
import com.smartcrop.advisory.dto.GenerateAdvisoryRequest;
import com.smartcrop.advisory.entity.Advisory;
import com.smartcrop.advisory.repository.AdvisoryRepository;
import com.smartcrop.auth.entity.Role;
import com.smartcrop.auth.entity.User;
import com.smartcrop.auth.repository.UserRepository;
import com.smartcrop.crop.entity.Crop;
import com.smartcrop.crop.repository.CropRepository;
import com.smartcrop.farmer.entity.Farmer;
import com.smartcrop.farmer.repository.FarmerRepository;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdvisoryPersistenceTest {

        private UserRepository userRepository;
        private FarmerRepository farmerRepository;
        private CropRepository cropRepository;
        private WeatherService weatherService;
        private AdvisoryRuleEngine ruleEngine;
        private AdvisoryRepository advisoryRepository;
        private AdvisoryService advisoryService;
        private Authentication authentication;

        private Farmer farmer;
        private Crop crop;

        @BeforeEach
        void setUp() {
                userRepository = mock(UserRepository.class);
                farmerRepository = mock(FarmerRepository.class);
                cropRepository = mock(CropRepository.class);
                weatherService = mock(WeatherService.class);
                ruleEngine = mock(AdvisoryRuleEngine.class);
                advisoryRepository = mock(AdvisoryRepository.class);

                advisoryService = new AdvisoryService(
                                userRepository,
                                farmerRepository,
                                cropRepository,
                                weatherService,
                                ruleEngine,
                                advisoryRepository);

                authentication = mock(Authentication.class);

                when(authentication.getName())
                                .thenReturn("farmer@example.com");

                User user = new User(
                                10L,
                                "Farmer",
                                "farmer@example.com",
                                null,
                                "hash",
                                Role.FARMER,
                                null,
                                null);

                farmer = new Farmer(
                                20L,
                                user,
                                "Pune",
                                "Maharashtra",
                                18.5,
                                73.8,
                                2.0);

                crop = new Crop(
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
        }

        @Test
        void generationPersistsAdvisoryAndRecommendations() {

                AdvisoryRecommendation recommendation = new AdvisoryRecommendation(
                                "RAINFALL",
                                "WARNING",
                                "Heavy rain",
                                "Delay irrigation.",
                                "Rain is expected.");

                WeatherForecastResponse weather = createValidWeather();

                when(weatherService.getForecast(authentication))
                                .thenReturn(weather);

                when(ruleEngine.generate(crop, weather))
                                .thenReturn(List.of(recommendation));

                when(advisoryRepository.save(any(Advisory.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                var response = advisoryService.generateAdvisory(
                                new GenerateAdvisoryRequest(30L, "en"),
                                authentication);

                var captor = org.mockito.ArgumentCaptor.forClass(Advisory.class);

                verify(advisoryRepository)
                                .save(captor.capture());

                Advisory saved = captor.getValue();

                assertEquals(crop, saved.getCrop());

                assertEquals(
                                "en",
                                saved.getLanguage());

                assertEquals(
                                1,
                                saved.getRecommendations().size());

                assertEquals(
                                "RAINFALL",
                                saved.getRecommendations()
                                                .get(0)
                                                .getCategory());

                assertEquals(
                                "WARNING",
                                saved.getRecommendations()
                                                .get(0)
                                                .getSeverity());

                assertEquals(
                                "Rice",
                                response.cropName());

                assertEquals(
                                1,
                                response.recommendations().size());
        }

        @Test
        void farmerAdvisoriesAreQueriedByFarmerOwnershipAndLanguage() {

                when(advisoryRepository
                                .findByCropFarmerIdAndLanguageOrderByGeneratedAtDesc(
                                                20L,
                                                "en"))
                                .thenReturn(List.of());

                assertEquals(
                                List.of(),
                                advisoryService.getMyAdvisories(
                                                authentication,
                                                "en"));

                verify(advisoryRepository)
                                .findByCropFarmerIdAndLanguageOrderByGeneratedAtDesc(
                                                20L,
                                                "en");
        }

        @Test
        void farmerCannotAccessAnotherFarmersAdvisory() {

                when(advisoryRepository
                                .findByIdAndCropFarmerIdAndLanguage(
                                                99L,
                                                20L,
                                                "en"))
                                .thenReturn(Optional.empty());

                assertThrows(
                                AdvisoryService.AdvisoryNotFoundException.class,
                                () -> advisoryService.getMyAdvisory(
                                                99L,
                                                authentication,
                                                "en"));
        }

        @Test
        void requestedLanguageIsUsedWhenFetchingAdvisories() {

                when(advisoryRepository
                                .findByCropFarmerIdAndLanguageOrderByGeneratedAtDesc(
                                                20L,
                                                "hi"))
                                .thenReturn(List.of());

                when(advisoryRepository
                                .findByCropFarmerIdAndLanguageOrderByGeneratedAtDesc(
                                                20L,
                                                "en"))
                                .thenReturn(List.of());

                assertEquals(
                                List.of(),
                                advisoryService.getMyAdvisories(
                                                authentication,
                                                "hi"));

                verify(advisoryRepository)
                                .findByCropFarmerIdAndLanguageOrderByGeneratedAtDesc(
                                                20L,
                                                "hi");
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