package com.smartcrop.advisory.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AdvisoryResponse(
        Long cropId,
        String cropName,
        String cropStage,
        LocalDateTime generatedAt,
        List<AdvisoryRecommendation> recommendations
) {
}
