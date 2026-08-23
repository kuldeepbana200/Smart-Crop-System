package com.smartcrop.distress.dto;

import com.smartcrop.distress.entity.AlertStatus;
import com.smartcrop.risk.dto.RiskFactor;

import java.time.LocalDateTime;
import java.util.List;

public record DistressAlertResponse(
        Long id,
        Long farmerId,
        Long cropId,
        String cropName,
        Integer riskScore,
        String riskLevel,
        String dominantFactor,
        List<RiskFactor> factors,
        String recommendedAction,
        AlertStatus status,
        Long assignedOfficerId,
        String officerNote,
        LocalDateTime createdAt,
        LocalDateTime acknowledgedAt,
        LocalDateTime resolvedAt) {
}
