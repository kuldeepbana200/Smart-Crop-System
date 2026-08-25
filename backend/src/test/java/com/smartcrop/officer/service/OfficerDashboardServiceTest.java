package com.smartcrop.officer.service;

import com.smartcrop.distress.entity.AlertStatus;
import com.smartcrop.distress.repository.DistressAlertRepository;
import com.smartcrop.intervention.entity.InterventionStatus;
import com.smartcrop.intervention.repository.InterventionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OfficerDashboardServiceTest {

    private DistressAlertRepository alertRepository;
    private InterventionRepository interventionRepository;
    private OfficerDashboardService dashboardService;

    @BeforeEach
    void setUp() {
        alertRepository = mock(DistressAlertRepository.class);
        interventionRepository = mock(InterventionRepository.class);
        dashboardService = new OfficerDashboardService(alertRepository, interventionRepository);
        when(alertRepository.countByStatus(AlertStatus.OPEN)).thenReturn(4L);
        when(alertRepository.countByStatus(AlertStatus.ACKNOWLEDGED)).thenReturn(2L);
        when(alertRepository.countByStatus(AlertStatus.RESOLVED)).thenReturn(6L);
        when(interventionRepository.countByStatusIn(
                List.of(InterventionStatus.PLANNED, InterventionStatus.IN_PROGRESS))).thenReturn(3L);
        when(interventionRepository.countByStatus(InterventionStatus.COMPLETED)).thenReturn(5L);
        when(alertRepository.findTop5ByStatusOrderByCreatedAtDesc(AlertStatus.OPEN)).thenReturn(List.of());
        when(alertRepository.findTop5ByStatusOrderByCreatedAtDesc(AlertStatus.ACKNOWLEDGED)).thenReturn(List.of());
        when(interventionRepository.findTop5ByStatusInOrderByCreatedAtDesc(
                List.of(InterventionStatus.PLANNED, InterventionStatus.IN_PROGRESS))).thenReturn(List.of());
        when(interventionRepository.findTop5ByStatusOrderByCreatedAtDesc(InterventionStatus.COMPLETED))
                .thenReturn(List.of());
    }

    @Test
    void dashboardContainsOfficerStatistics() {
        var dashboard = dashboardService.getDashboard();

        assertEquals(4L, dashboard.summary().openAlerts());
        assertEquals(2L, dashboard.summary().acknowledgedAlerts());
        assertEquals(6L, dashboard.summary().resolvedAlerts());
        assertEquals(3L, dashboard.summary().activeInterventions());
        assertEquals(5L, dashboard.summary().completedInterventions());
    }

    @Test
    void dashboardUsesOfficerQueriesNotFarmerScopedQueries() {
        dashboardService.getDashboard();

        verify(alertRepository).findTop5ByStatusOrderByCreatedAtDesc(AlertStatus.OPEN);
        verify(alertRepository).findTop5ByStatusOrderByCreatedAtDesc(AlertStatus.ACKNOWLEDGED);
        verify(interventionRepository).findTop5ByStatusInOrderByCreatedAtDesc(
                List.of(InterventionStatus.PLANNED, InterventionStatus.IN_PROGRESS));
    }
}
