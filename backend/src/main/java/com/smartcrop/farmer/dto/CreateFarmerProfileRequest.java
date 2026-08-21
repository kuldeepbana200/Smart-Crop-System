package com.smartcrop.farmer.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateFarmerProfileRequest(
        @NotBlank(message = "District is required") @Size(max = 100, message = "District must not exceed 100 characters") String district,

        @NotBlank(message = "State is required") @Size(max = 100, message = "State must not exceed 100 characters") String state,

        @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90") @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90") Double latitude,

        @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180") @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180") Double longitude,

        @DecimalMin(value = "0.0", inclusive = false, message = "Land area must be greater than zero") Double landArea) {
}
