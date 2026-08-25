package com.smartcrop.market.client;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface MarketDataProvider {

    List<MarketPriceData> getPrices();

    record MarketPriceData(
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
}
