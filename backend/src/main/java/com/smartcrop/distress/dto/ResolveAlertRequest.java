package com.smartcrop.distress.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResolveAlertRequest(
        @JsonProperty("officerNote")
        @NotBlank(message = "Officer note is required") @Size(max = 2000, message = "Officer note must not exceed 2000 characters") String note) {
}
