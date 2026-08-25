package com.smartcrop.officer.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AssignAlertRequest(
        @NotNull(message = "Officer ID is required") @Positive(message = "Officer ID must be positive") Long officerId) {
}
