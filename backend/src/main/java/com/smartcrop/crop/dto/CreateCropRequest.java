package com.smartcrop.crop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateCropRequest(
        @NotBlank(message = "Crop name is required") @Size(max = 100, message = "Crop name must not exceed 100 characters") String cropName,

        @Size(max = 100, message = "Crop stage must not exceed 100 characters") String cropStage,

        @NotNull(message = "Sowing date is required") LocalDate sowingDate,

        @NotNull(message = "Expected harvest date is required") LocalDate expectedHarvestDate) {
}
