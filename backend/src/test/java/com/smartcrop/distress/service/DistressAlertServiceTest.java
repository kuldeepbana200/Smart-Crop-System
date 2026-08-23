package com.smartcrop.distress.service;

import com.smartcrop.auth.entity.Role;
import com.smartcrop.auth.entity.User;
import com.smartcrop.auth.repository.UserRepository;
import com.smartcrop.crop.entity.Crop;
import com.smartcrop.distress.dto.AcknowledgeAlertRequest;
import com.smartcrop.distress.dto.ResolveAlertRequest;
import com.smartcrop.distress.entity.AlertStatus;
import com.smartcrop.distress.entity.DistressAlert;
import com.smartcrop.distress.repository.DistressAlertRepository;
import com.smartcrop.farmer.entity.Farmer;
import com.smartcrop.farmer.repository.FarmerRepository;
import com.smartcrop.risk.dto.RiskAssessmentResponse;
import com.smartcrop.risk.dto.RiskFactor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DistressAlertServiceTest {

    private DistressAlertRepository alertRepository;
    private UserRepository userRepository;
    private FarmerRepository farmerRepository;
    private DistressAlertService alertService;
    private Farmer farmer;
    private Crop crop;
    private RiskAssessmentResponse highRisk;

    @BeforeEach
    void setUp() {
        alertRepository = mock(DistressAlertRepository.class);
        userRepository = mock(UserRepository.class);
        farmerRepository = mock(FarmerRepository.class);
        alertService = new DistressAlertService(alertRepository, userRepository, farmerRepository);
        User user = new User(10L, "Farmer", "farmer@example.com", null, "hash", Role.FARMER, null, null);
        farmer = new Farmer(20L, user, "District", "State", 1.0, 2.0, 3.0);
        crop = new Crop(30L, farmer, "Rice", "FLOWERING", null, null, null);
        highRisk = assessment("HIGH", 60);
    }

    @Test
    void highRiskCreatesAlert() {
        when(alertRepository.findByFarmerIdAndCropIdAndConditionKeyAndStatus(
                20L, 30L, "HEAVY_RAINFALL", AlertStatus.OPEN)).thenReturn(Optional.empty());
        when(alertRepository.findByFarmerIdAndCropIdAndConditionKeyAndStatus(
                20L, 30L, "HEAVY_RAINFALL", AlertStatus.ACKNOWLEDGED)).thenReturn(Optional.empty());

        alertService.createIfRequired(farmer, crop, highRisk);

        verify(alertRepository).save(any(DistressAlert.class));
    }

    @Test
    void criticalRiskCreatesAlert() {
        when(alertRepository.findByFarmerIdAndCropIdAndConditionKeyAndStatus(
                20L, 30L, "HEAVY_RAINFALL", AlertStatus.OPEN)).thenReturn(Optional.empty());
        when(alertRepository.findByFarmerIdAndCropIdAndConditionKeyAndStatus(
                20L, 30L, "HEAVY_RAINFALL", AlertStatus.ACKNOWLEDGED)).thenReturn(Optional.empty());

        alertService.createIfRequired(farmer, crop, assessment("CRITICAL", 90));

        verify(alertRepository).save(any(DistressAlert.class));
    }

    @Test
    void lowAndModerateRiskDoNotCreateAlerts() {
        alertService.createIfRequired(farmer, crop, assessment("LOW", 10));
        alertService.createIfRequired(farmer, crop, assessment("MODERATE", 30));

        verify(alertRepository, never()).save(any(DistressAlert.class));
    }

    @Test
    void openAlertPreventsDuplicate() {
        DistressAlert existing = alert(AlertStatus.OPEN);
        when(alertRepository.findByFarmerIdAndCropIdAndConditionKeyAndStatus(
                20L, 30L, "HEAVY_RAINFALL", AlertStatus.OPEN)).thenReturn(Optional.of(existing));

        alertService.createIfRequired(farmer, crop, highRisk);

        verify(alertRepository, never()).save(any(DistressAlert.class));
    }

    @Test
    void acknowledgedAlertPreventsDuplicate() {
        DistressAlert existing = alert(AlertStatus.ACKNOWLEDGED);
        when(alertRepository.findByFarmerIdAndCropIdAndConditionKeyAndStatus(
                20L, 30L, "HEAVY_RAINFALL", AlertStatus.OPEN)).thenReturn(Optional.empty());
        when(alertRepository.findByFarmerIdAndCropIdAndConditionKeyAndStatus(
                20L, 30L, "HEAVY_RAINFALL", AlertStatus.ACKNOWLEDGED)).thenReturn(Optional.of(existing));

        alertService.createIfRequired(farmer, crop, highRisk);

        verify(alertRepository, never()).save(any(DistressAlert.class));
    }

    @Test
    void resolvedAlertAllowsNewAlert() {
        when(alertRepository.findByFarmerIdAndCropIdAndConditionKeyAndStatus(
                20L, 30L, "HEAVY_RAINFALL", AlertStatus.OPEN)).thenReturn(Optional.empty());
        when(alertRepository.findByFarmerIdAndCropIdAndConditionKeyAndStatus(
                20L, 30L, "HEAVY_RAINFALL", AlertStatus.ACKNOWLEDGED)).thenReturn(Optional.empty());

        alertService.createIfRequired(farmer, crop, highRisk);

        verify(alertRepository).save(any(DistressAlert.class));
    }

    @Test
    void farmerOnlyReceivesOwnAlerts() {
        Authentication authentication = authentication();
        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(farmer.getUser()));
        when(farmerRepository.findByUserId(10L)).thenReturn(Optional.of(farmer));
        when(alertRepository.findByFarmerIdOrderByCreatedAtDesc(20L)).thenReturn(List.of());

        assertEquals(List.of(), alertService.getFarmerAlerts(authentication));
        verify(alertRepository).findByFarmerIdOrderByCreatedAtDesc(20L);
    }

    @Test
    void farmerCannotAccessAnotherFarmersAlert() {
        Authentication authentication = authentication();
        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(farmer.getUser()));
        when(farmerRepository.findByUserId(10L)).thenReturn(Optional.of(farmer));
        when(alertRepository.findByIdAndFarmerId(99L, 20L)).thenReturn(Optional.empty());

        assertThrows(DistressAlertService.AlertNotFoundException.class,
                () -> alertService.getFarmerAlert(99L, authentication));
    }

    @Test
    void lifecycleTransitionsAreEnforced() {
        DistressAlert open = alert(AlertStatus.OPEN);
        when(alertRepository.findById(1L)).thenReturn(Optional.of(open));
        when(alertRepository.save(open)).thenReturn(open);
        alertService.acknowledge(1L, new AcknowledgeAlertRequest("called"));
        assertEquals(AlertStatus.ACKNOWLEDGED, open.getStatus());

        DistressAlert acknowledged = alert(AlertStatus.ACKNOWLEDGED);
        when(alertRepository.findById(2L)).thenReturn(Optional.of(acknowledged));
        when(alertRepository.save(acknowledged)).thenReturn(acknowledged);
        alertService.resolve(2L, new ResolveAlertRequest("stable"));
        assertEquals(AlertStatus.RESOLVED, acknowledged.getStatus());
    }

    @Test
    void resolvedAlertCannotBeAcknowledgedOrResolvedAgain() {
        DistressAlert resolved = alert(AlertStatus.RESOLVED);
        when(alertRepository.findById(1L)).thenReturn(Optional.of(resolved));

        assertThrows(DistressAlertService.InvalidAlertTransitionException.class,
                () -> alertService.acknowledge(1L, new AcknowledgeAlertRequest("again")));
        assertThrows(DistressAlertService.InvalidAlertTransitionException.class,
                () -> alertService.resolve(1L, new ResolveAlertRequest("again")));
    }

    private RiskAssessmentResponse assessment(String level, int score) {
        return new RiskAssessmentResponse(
                30L, "Rice", "FLOWERING", score, level,
                List.of(new RiskFactor("HEAVY_RAINFALL", "HIGH", 30, "rain")),
                "Inspect drainage.", null);
    }

    private DistressAlert alert(AlertStatus status) {
        return new DistressAlert(1L, farmer, crop, null, 60, "HIGH", "HEAVY_RAINFALL",
                "HEAVY_RAINFALL", "", "Inspect drainage.", status, null, null, null, null);
    }

    private Authentication authentication() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("farmer@example.com");
        return authentication;
    }
}
