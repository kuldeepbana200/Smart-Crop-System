package com.smartcrop.crop.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CropResponse(
        Long id,
        Long farmerId,
        String cropName,
        String cropStage,
        LocalDate sowingDate,
        LocalDate expectedHarvestDate,
        LocalDateTime createdAt) {
}
