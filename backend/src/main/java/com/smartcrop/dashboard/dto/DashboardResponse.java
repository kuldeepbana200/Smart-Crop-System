package com.smartcrop.dashboard.dto;

import com.smartcrop.advisory.entity.Advisory;
import com.smartcrop.crop.entity.Crop;
import com.smartcrop.distress.entity.DistressAlert;
import com.smartcrop.intervention.entity.Intervention;
import com.smartcrop.notification.entity.Notification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record DashboardResponse(
        FarmerSummary farmer,
        SummaryStatistics statistics,
        List<CropSummary> recentCrops,
        List<AlertSummary> recentAlerts,
        List<AdvisorySummary> recentAdvisories,
        List<NotificationSummary> recentNotifications) {

    public record FarmerSummary(Long id, String name, String district, String state) {
    }

    public record SummaryStatistics(
            long totalCrops,
            long openDistressAlerts,
            long acknowledgedDistressAlerts,
            long resolvedDistressAlerts,
            long activeInterventions,
            long unreadNotifications,
            long totalAdvisories) {
    }

    public record CropSummary(
            Long id,
            String cropName,
            String cropStage,
            LocalDate sowingDate,
            LocalDate expectedHarvestDate,
            LocalDateTime createdAt) {

        public static CropSummary from(Crop crop) {
            return new CropSummary(crop.getId(), crop.getCropName(), crop.getCropStage(),
                    crop.getSowingDate(), crop.getExpectedHarvestDate(), crop.getCreatedAt());
        }
    }

    public record AlertSummary(
            Long id,
            Long cropId,
            String cropName,
            Integer riskScore,
            String riskLevel,
            String dominantFactor,
            String recommendedAction,
            String status,
            LocalDateTime createdAt,
            LocalDateTime acknowledgedAt,
            LocalDateTime resolvedAt) {

        public static AlertSummary from(DistressAlert alert) {
            return new AlertSummary(alert.getId(), alert.getCrop().getId(), alert.getCrop().getCropName(),
                    alert.getRiskScore(), alert.getRiskLevel(), alert.getDominantFactor(),
                    alert.getRecommendedAction(), alert.getStatus().name(), alert.getCreatedAt(),
                    alert.getAcknowledgedAt(), alert.getResolvedAt());
        }
    }

    public record AdvisorySummary(
            Long id,
            Long cropId,
            String cropName,
            String cropStage,
            LocalDateTime generatedAt,
            int recommendationCount) {

        public static AdvisorySummary from(Advisory advisory) {
            return new AdvisorySummary(advisory.getId(), advisory.getCrop().getId(),
                    advisory.getCrop().getCropName(), advisory.getCrop().getCropStage(),
                    advisory.getGeneratedAt(), advisory.getRecommendations().size());
        }
    }

    public record NotificationSummary(
            Long id,
            String type,
            String title,
            String message,
            String status,
            LocalDateTime createdAt,
            LocalDateTime readAt,
            Long distressAlertId,
            Long interventionId) {

        public static NotificationSummary from(Notification notification) {
            return new NotificationSummary(notification.getId(), notification.getType().name(),
                    notification.getTitle(), notification.getMessage(), notification.getStatus().name(),
                    notification.getCreatedAt(), notification.getReadAt(),
                    notification.getDistressAlert() == null ? null : notification.getDistressAlert().getId(),
                    notification.getIntervention() == null ? null : notification.getIntervention().getId());
        }
    }
}
