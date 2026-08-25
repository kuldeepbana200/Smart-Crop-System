package com.smartcrop.officer.dto;

import com.smartcrop.distress.entity.DistressAlert;
import com.smartcrop.intervention.entity.Intervention;

import java.time.LocalDateTime;
import java.util.List;

public record OfficerDashboardResponse(
        Summary summary,
        List<AlertSummary> recentOpenAlerts,
        List<AlertSummary> recentAcknowledgedAlerts,
        List<InterventionSummary> activeInterventions,
        List<InterventionSummary> recentlyCompletedInterventions) {

    public record Summary(
            long openAlerts,
            long acknowledgedAlerts,
            long resolvedAlerts,
            long activeInterventions,
            long completedInterventions) {
    }

    public record AlertSummary(
            Long id,
            Long farmerId,
            Long cropId,
            String cropName,
            Integer riskScore,
            String riskLevel,
            String dominantFactor,
            String recommendedAction,
            String status,
            Long assignedOfficerId,
            LocalDateTime createdAt) {

        public static AlertSummary from(DistressAlert alert) {
            return new AlertSummary(alert.getId(), alert.getFarmer().getId(), alert.getCrop().getId(),
                    alert.getCrop().getCropName(), alert.getRiskScore(), alert.getRiskLevel(),
                    alert.getDominantFactor(), alert.getRecommendedAction(), alert.getStatus().name(),
                    alert.getAssignedOfficer() == null ? null : alert.getAssignedOfficer().getId(),
                    alert.getCreatedAt());
        }
    }

    public record InterventionSummary(
            Long id,
            Long distressAlertId,
            Long farmerId,
            Long cropId,
            String cropName,
            Long officerId,
            String type,
            String description,
            String status,
            LocalDateTime createdAt,
            LocalDateTime completedAt) {

        public static InterventionSummary from(Intervention intervention) {
            DistressAlert alert = intervention.getDistressAlert();
            return new InterventionSummary(intervention.getId(), alert.getId(), alert.getFarmer().getId(),
                    alert.getCrop().getId(), alert.getCrop().getCropName(), intervention.getOfficer().getId(),
                    intervention.getType().name(), intervention.getDescription(),
                    intervention.getStatus().name(), intervention.getCreatedAt(), intervention.getCompletedAt());
        }
    }
}
