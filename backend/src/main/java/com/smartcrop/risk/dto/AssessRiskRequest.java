package com.smartcrop.risk.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AssessRiskRequest(
        @NotNull(message = "Crop ID is required") @Positive(message = "Crop ID must be positive") Long cropId) {
}
