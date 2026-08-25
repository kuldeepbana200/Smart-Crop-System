package com.smartcrop.market.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MarketPriceResponse(
        Long id,
        Long marketId,
        String marketName,
        String district,
        String state,
        String cropName,
        String variety,
        String unit,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        BigDecimal modalPrice,
        String currency,
        LocalDateTime observedAt) {
}
