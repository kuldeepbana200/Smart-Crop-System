package com.smartcrop.intervention.dto;

import com.smartcrop.intervention.entity.InterventionStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateInterventionRequest(
        @NotNull(message = "Intervention status is required") InterventionStatus status,
        @Size(max = 4000, message = "Intervention description must not exceed 4000 characters")
        String description) {
}
