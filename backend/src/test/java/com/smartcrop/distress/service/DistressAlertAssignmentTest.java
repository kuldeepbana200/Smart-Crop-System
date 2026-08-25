package com.smartcrop.distress.service;

import com.smartcrop.auth.entity.Role;
import com.smartcrop.auth.entity.User;
import com.smartcrop.auth.repository.UserRepository;
import com.smartcrop.crop.entity.Crop;
import com.smartcrop.distress.dto.AcknowledgeAlertRequest;
import com.smartcrop.distress.entity.AlertStatus;
import com.smartcrop.distress.entity.DistressAlert;
import com.smartcrop.distress.repository.DistressAlertRepository;
import com.smartcrop.farmer.entity.Farmer;
import com.smartcrop.farmer.repository.FarmerRepository;
import com.smartcrop.officer.dto.AssignAlertRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DistressAlertAssignmentTest {

    private DistressAlertRepository alertRepository;
    private UserRepository userRepository;
    private FarmerRepository farmerRepository;
    private DistressAlertService alertService;
    private DistressAlert alert;
    private User assignedOfficer;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        alertRepository = mock(DistressAlertRepository.class);
        userRepository = mock(UserRepository.class);
        farmerRepository = mock(FarmerRepository.class);
        alertService = new DistressAlertService(alertRepository, userRepository, farmerRepository);
        User farmerUser = new User(1L, "Farmer", "farmer@example.com", null, "hash", Role.FARMER, null, null);
        Farmer farmer = new Farmer(10L, farmerUser, "Pune", "Maharashtra", 18.5, 73.8, 2.0);
        Crop crop = new Crop(20L, farmer, "Rice", "FLOWERING", null, null, null);
        alert = new DistressAlert(30L, farmer, crop, null, 80, "CRITICAL", "HEAT", "HEAT",
                "SEhFVA:SElHSA:25:TW9uaXRvcg", "Monitor.", AlertStatus.OPEN, null, null, null, null);
        assignedOfficer = new User(2L, "Officer", "officer@example.com", null, "hash", Role.OFFICER, null, null);
        authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("officer@example.com");
        when(alertRepository.findById(30L)).thenReturn(Optional.of(alert));
        when(alertRepository.save(any(DistressAlert.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findByEmail("officer@example.com")).thenReturn(Optional.of(assignedOfficer));
    }

    @Test
    void officerAssignmentPersistsWithoutChangingStatus() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(assignedOfficer));

        var response = alertService.assign(30L, new AssignAlertRequest(2L));

        assertEquals(AlertStatus.OPEN, alert.getStatus());
        assertEquals(2L, alert.getAssignedOfficer().getId());
        assertEquals(2L, response.assignedOfficerId());
        verify(alertRepository).save(alert);
    }

    @Test
    void nonexistentOfficerIsRejected() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(DistressAlertService.AssignedOfficerNotFoundException.class,
                () -> alertService.assign(30L, new AssignAlertRequest(99L)));
    }

    @Test
    void farmerCannotBeAssigned() {
        User farmer = new User(3L, "Farmer", "farmer2@example.com", null, "hash", Role.FARMER, null, null);
        when(userRepository.findById(3L)).thenReturn(Optional.of(farmer));

        assertThrows(DistressAlertService.InvalidAssignedOfficerException.class,
                () -> alertService.assign(30L, new AssignAlertRequest(3L)));
    }

    @Test
    void assignedOfficerCanAcknowledge() {
        when(userRepository.findByEmail("officer@example.com")).thenReturn(Optional.of(assignedOfficer));
        alert.assignOfficer(assignedOfficer);

        var response = alertService.acknowledge(
                30L, new AcknowledgeAlertRequest("Reviewed."), authentication);

        assertEquals(AlertStatus.ACKNOWLEDGED, response.status());
        assertEquals(2L, response.assignedOfficerId());
    }

    @Test
    void differentOfficerCannotTakeOverExplicitAssignment() {
        User differentOfficer = new User(3L, "Other", "other@example.com", null, "hash", Role.OFFICER, null, null);
        alert.assignOfficer(assignedOfficer);
        when(authentication.getName()).thenReturn("other@example.com");
        when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(differentOfficer));

        assertThrows(DistressAlertService.AssignedOfficerConflictException.class,
                () -> alertService.acknowledge(
                        30L, new AcknowledgeAlertRequest("Reviewing."), authentication));
        assertEquals(AlertStatus.OPEN, alert.getStatus());
    }
}
