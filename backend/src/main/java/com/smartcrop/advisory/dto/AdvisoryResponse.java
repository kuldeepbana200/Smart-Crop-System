package com.smartcrop.advisory.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record AdvisoryResponse(
        Long id,
        Long cropId,
        String cropName,
        String cropStage,
        LocalDate sowingDate,
        LocalDate expectedHarvestDate,
        String lifecycle,
        LocalDateTime generatedAt,
        List<AdvisoryRecommendation> recommendations) {
}
