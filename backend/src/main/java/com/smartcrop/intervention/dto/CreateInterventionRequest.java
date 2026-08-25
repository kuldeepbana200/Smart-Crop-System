package com.smartcrop.intervention.dto;

import com.smartcrop.intervention.entity.InterventionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateInterventionRequest(
        @NotNull(message = "Intervention type is required") InterventionType type,
        @NotBlank(message = "Intervention description is required")
        @Size(max = 4000, message = "Intervention description must not exceed 4000 characters")
        String description) {
}
