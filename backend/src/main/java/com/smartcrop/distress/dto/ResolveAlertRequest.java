package com.smartcrop.distress.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResolveAlertRequest(
        @NotBlank(message = "Officer note is required") @Size(max = 2000, message = "Officer note must not exceed 2000 characters") String note) {
}
