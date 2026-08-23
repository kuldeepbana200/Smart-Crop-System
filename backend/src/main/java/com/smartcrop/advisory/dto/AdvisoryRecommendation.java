package com.smartcrop.advisory.dto;

public record AdvisoryRecommendation(
        String category,
        String severity,
        String title,
        String recommendation,
        String reason
) {
}
