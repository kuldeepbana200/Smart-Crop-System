package com.smartcrop.officer.service;

import com.smartcrop.distress.entity.AlertStatus;
import com.smartcrop.distress.repository.DistressAlertRepository;
import com.smartcrop.intervention.entity.InterventionStatus;
import com.smartcrop.intervention.repository.InterventionRepository;
import com.smartcrop.officer.dto.OfficerDashboardResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OfficerDashboardService {

    private final DistressAlertRepository alertRepository;
    private final InterventionRepository interventionRepository;

    public OfficerDashboardService(
            DistressAlertRepository alertRepository,
            InterventionRepository interventionRepository) {
        this.alertRepository = alertRepository;
        this.interventionRepository = interventionRepository;
    }

    @Transactional(readOnly = true)
    public OfficerDashboardResponse getDashboard() {
        return new OfficerDashboardResponse(
                new OfficerDashboardResponse.Summary(
                        alertRepository.countByStatus(AlertStatus.OPEN),
                        alertRepository.countByStatus(AlertStatus.ACKNOWLEDGED),
                        alertRepository.countByStatus(AlertStatus.RESOLVED),
                        interventionRepository.countByStatusIn(
                                List.of(InterventionStatus.PLANNED, InterventionStatus.IN_PROGRESS)),
                        interventionRepository.countByStatus(InterventionStatus.COMPLETED)),
                alertRepository.findTop5ByStatusOrderByCreatedAtDesc(AlertStatus.OPEN).stream()
                        .map(OfficerDashboardResponse.AlertSummary::from).toList(),
                alertRepository.findTop5ByStatusOrderByCreatedAtDesc(AlertStatus.ACKNOWLEDGED).stream()
                        .map(OfficerDashboardResponse.AlertSummary::from).toList(),
                interventionRepository.findTop5ByStatusInOrderByCreatedAtDesc(
                        List.of(InterventionStatus.PLANNED, InterventionStatus.IN_PROGRESS)).stream()
                        .map(OfficerDashboardResponse.InterventionSummary::from).toList(),
                interventionRepository.findTop5ByStatusOrderByCreatedAtDesc(InterventionStatus.COMPLETED).stream()
                        .map(OfficerDashboardResponse.InterventionSummary::from).toList());
    }
}
