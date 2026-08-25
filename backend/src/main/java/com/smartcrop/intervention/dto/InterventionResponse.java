package com.smartcrop.intervention.dto;

import com.smartcrop.intervention.entity.InterventionStatus;
import com.smartcrop.intervention.entity.InterventionType;

import java.time.LocalDateTime;

public record InterventionResponse(
        Long id,
        Long distressAlertId,
        Long farmerId,
        Long cropId,
        String cropName,
        Long officerId,
        InterventionType type,
        String description,
        InterventionStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime completedAt) {
}
