package com.smartcrop.risk.dto;

import java.time.LocalDateTime;
import java.util.List;

public record RiskAssessmentResponse(
        Long cropId,
        String cropName,
        String cropStage,
        Integer riskScore,
        String riskLevel,
        List<RiskFactor> factors,
        String recommendedAction,
        LocalDateTime assessedAt) {
}
