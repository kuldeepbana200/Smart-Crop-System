package com.smartcrop.risk.dto;

public record RiskFactor(
        String type,
        String severity,
        Integer contribution,
        String reason) {
}
