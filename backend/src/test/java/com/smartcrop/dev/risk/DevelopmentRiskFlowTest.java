package com.smartcrop.dev.risk;

import com.smartcrop.auth.entity.Role;
import com.smartcrop.auth.entity.User;
import com.smartcrop.auth.repository.UserRepository;
import com.smartcrop.crop.entity.Crop;
import com.smartcrop.crop.repository.CropRepository;
import com.smartcrop.distress.entity.AlertStatus;
import com.smartcrop.distress.entity.DistressAlert;
import com.smartcrop.distress.repository.DistressAlertRepository;
import com.smartcrop.distress.service.DistressAlertService;
import com.smartcrop.dev.weather.DevelopmentWeatherService;
import com.smartcrop.farmer.entity.Farmer;
import com.smartcrop.farmer.repository.FarmerRepository;
import com.smartcrop.risk.dto.AssessRiskRequest;
import com.smartcrop.risk.dto.RiskAssessmentResponse;
import com.smartcrop.risk.engine.RiskEngine;
import com.smartcrop.risk.service.RiskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DevelopmentRiskFlowTest {

    private UserRepository userRepository;
    private FarmerRepository farmerRepository;
    private CropRepository cropRepository;
    private DistressAlertRepository alertRepository;
    private RiskService riskService;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        farmerRepository = mock(FarmerRepository.class);
        cropRepository = mock(CropRepository.class);
        alertRepository = mock(DistressAlertRepository.class);
        authentication = mock(Authentication.class);

        User user = new User(10L, "Farmer", "farmer@example.com", null, "hash", Role.FARMER, null, null);
        Farmer farmer = new Farmer(20L, user, "District", "State", 1.0, 2.0, 3.0);
        Crop crop = new Crop(30L, farmer, "Rice", "VEGETATIVE", null, null, null);
        when(authentication.getName()).thenReturn("farmer@example.com");
        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(user));
        when(farmerRepository.findByUserId(10L)).thenReturn(Optional.of(farmer));
        when(cropRepository.findByIdAndFarmerId(30L, 20L)).thenReturn(Optional.of(crop));
        when(alertRepository.findByFarmerIdAndCropIdAndConditionKeyAndStatus(
                20L, 30L, "EXTREME_HEAT|HEAVY_RAINFALL", AlertStatus.OPEN))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(new DistressAlert()));
        when(alertRepository.findByFarmerIdAndCropIdAndConditionKeyAndStatus(
                20L, 30L, "EXTREME_HEAT|HEAVY_RAINFALL", AlertStatus.ACKNOWLEDGED))
                .thenReturn(Optional.empty());
        when(alertRepository.save(any(DistressAlert.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DevelopmentWeatherService developmentWeatherService = new DevelopmentWeatherService(
                userRepository, farmerRepository, mock(com.smartcrop.weather.client.OpenMeteoClient.class));
        riskService = new RiskService(
                userRepository,
                farmerRepository,
                cropRepository,
                developmentWeatherService,
                new RiskEngine(),
                new DistressAlertService(alertRepository, userRepository, farmerRepository));
    }

    @Test
    void assessesAnyOwnedCropAndCreatesOneOpenAlert() {
        RiskAssessmentResponse result = riskService.assessRisk(
                new AssessRiskRequest(30L), authentication);
        riskService.assessRisk(new AssessRiskRequest(30L), authentication);

        assertEquals(55, result.riskScore());
        assertEquals("HIGH", result.riskLevel());
        ArgumentCaptor<DistressAlert> captor = ArgumentCaptor.forClass(DistressAlert.class);
        verify(alertRepository).save(captor.capture());
        assertEquals(AlertStatus.OPEN, captor.getValue().getStatus());
    }
}
