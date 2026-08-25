package com.smartcrop.intervention.service;

import com.smartcrop.auth.entity.Role;
import com.smartcrop.auth.entity.User;
import com.smartcrop.auth.repository.UserRepository;
import com.smartcrop.crop.entity.Crop;
import com.smartcrop.distress.entity.AlertStatus;
import com.smartcrop.distress.entity.DistressAlert;
import com.smartcrop.distress.repository.DistressAlertRepository;
import com.smartcrop.farmer.entity.Farmer;
import com.smartcrop.intervention.dto.CreateInterventionRequest;
import com.smartcrop.intervention.dto.InterventionResponse;
import com.smartcrop.intervention.dto.UpdateInterventionRequest;
import com.smartcrop.intervention.entity.Intervention;
import com.smartcrop.intervention.entity.InterventionStatus;
import com.smartcrop.intervention.entity.InterventionType;
import com.smartcrop.intervention.repository.InterventionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InterventionServiceTest {

    private InterventionRepository interventionRepository;
    private DistressAlertRepository distressAlertRepository;
    private UserRepository userRepository;
    private InterventionService interventionService;
    private Authentication authentication;
    private User officer;
    private DistressAlert alert;

    @BeforeEach
    void setUp() {
        interventionRepository = mock(InterventionRepository.class);
        distressAlertRepository = mock(DistressAlertRepository.class);
        userRepository = mock(UserRepository.class);
        interventionService = new InterventionService(
                interventionRepository, distressAlertRepository, userRepository);

        authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("officer@example.com");
        officer = new User(2L, "Officer", "officer@example.com", null, "hash", Role.OFFICER, null, null);
        when(userRepository.findByEmail("officer@example.com")).thenReturn(Optional.of(officer));

        User farmerUser = new User(1L, "Farmer", "farmer@example.com", null, "hash", Role.FARMER, null, null);
        Farmer farmer = new Farmer(10L, farmerUser, "Pune", "Maharashtra", 18.5, 73.8, 2.0);
        Crop crop = new Crop(20L, farmer, "Rice", "FLOWERING", null, null, null);
        alert = new DistressAlert(
                30L, farmer, crop, null, 80, "CRITICAL", "EXTREME_HEAT", "EXTREME_HEAT",
                "ZW5jb2RlZA:SElHSA:25:aGVhdA", "Inspect crop.", AlertStatus.OPEN, null,
                null, null, null);
    }

    @Test
    void officerCreatesInterventionAndDerivesFarmerAndCropFromAlert() {
        when(distressAlertRepository.findById(30L)).thenReturn(Optional.of(alert));
        when(interventionRepository.save(any(Intervention.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        InterventionResponse response = interventionService.create(
                30L,
                new CreateInterventionRequest(InterventionType.FIELD_VISIT, "Visit the field."),
                authentication);

        assertEquals(30L, response.distressAlertId());
        assertEquals(10L, response.farmerId());
        assertEquals(20L, response.cropId());
        assertEquals("Rice", response.cropName());
        assertEquals(2L, response.officerId());
        assertEquals(InterventionStatus.PLANNED, response.status());
        verify(interventionRepository).save(any(Intervention.class));
    }

    @Test
    void invalidAlertIsRejected() {
        when(distressAlertRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(InterventionService.DistressAlertNotFoundException.class,
                () -> interventionService.create(
                        99L,
                        new CreateInterventionRequest(InterventionType.PHONE_CALL, "Call farmer."),
                        authentication));
    }

    @Test
    void farmerCannotUseService() {
        User farmer = new User(1L, "Farmer", "farmer@example.com", null, "hash", Role.FARMER, null, null);
        when(authentication.getName()).thenReturn("farmer@example.com");
        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(farmer));

        assertThrows(InterventionService.OfficerAccessDeniedException.class,
                () -> interventionService.create(
                        30L,
                        new CreateInterventionRequest(InterventionType.PHONE_CALL, "Call farmer."),
                        authentication));
    }

    @Test
    void statusUpdateSetsCompletedAt() {
        Intervention intervention = new Intervention(
                40L, alert, officer, InterventionType.FIELD_VISIT, "Visit the field.",
                InterventionStatus.IN_PROGRESS, null, null, null);
        when(interventionRepository.findById(40L)).thenReturn(Optional.of(intervention));
        when(interventionRepository.save(any(Intervention.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        InterventionResponse response = interventionService.update(
                40L,
                new UpdateInterventionRequest(InterventionStatus.COMPLETED, "Completed the visit."),
                authentication);

        assertEquals(InterventionStatus.COMPLETED, response.status());
        assertEquals("Completed the visit.", response.description());
        assertEquals(2L, response.officerId());
        assertEquals(InterventionStatus.COMPLETED, intervention.getStatus());
        assertNotNull(intervention.getCompletedAt());
    }

    @Test
    void completedInterventionCannotTransitionAgain() {
        Intervention intervention = new Intervention(
                40L, alert, officer, InterventionType.FIELD_VISIT, "Visit complete.",
                InterventionStatus.COMPLETED, null, null, null);
        when(interventionRepository.findById(40L)).thenReturn(Optional.of(intervention));

        assertThrows(InterventionService.InvalidInterventionTransitionException.class,
                () -> interventionService.update(
                        40L,
                        new UpdateInterventionRequest(InterventionStatus.CANCELLED, null),
                        authentication));
    }
}
