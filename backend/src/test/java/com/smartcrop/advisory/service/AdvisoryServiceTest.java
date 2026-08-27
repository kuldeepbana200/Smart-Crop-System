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
import com.smartcrop.weather.service.WeatherService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdvisoryServiceTest {

    private UserRepository userRepository;
    private FarmerRepository farmerRepository;
    private CropRepository cropRepository;
    private WeatherService weatherService;
    private AdvisoryRuleEngine ruleEngine;
    private AdvisoryRepository advisoryRepository;
    private AdvisoryService advisoryService;
    private Authentication authentication;

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

        AdvisoryRecommendation recommendation = new AdvisoryRecommendation(
                "IRRIGATION",
                "ADVISORY",
                "Irrigation required",
                "Monitor soil moisture.",
                "Low rainfall is expected.");

        when(userRepository.findByEmail("farmer@example.com"))
                .thenReturn(Optional.of(user));

        when(farmerRepository.findByUserId(10L))
                .thenReturn(Optional.of(farmer));

        when(cropRepository.findByIdAndFarmerId(30L, 20L))
                .thenReturn(Optional.of(crop));

        when(weatherService.getForecast(authentication))
                .thenReturn(null);

        when(ruleEngine.generate(crop, null))
                .thenReturn(List.of(recommendation));

        /*
         * The service saves the generated Advisory.
         * Return the same entity passed to save().
         */
        when(advisoryRepository.save(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        advisoryService.generateAdvisory(
                new GenerateAdvisoryRequest(30L),
                authentication);

        verify(cropRepository)
                .findByIdAndFarmerId(30L, 20L);

        verify(weatherService)
                .getForecast(authentication);

        verify(advisoryRepository)
                .save(org.mockito.ArgumentMatchers.any());
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
                        new GenerateAdvisoryRequest(30L),
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
                        new GenerateAdvisoryRequest(99L),
                        authentication));

        assertEquals(
                AdvisoryService.CropNotFoundException.class,
                exception.getClass());
    }
}