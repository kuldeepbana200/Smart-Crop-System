package com.smartcrop.dev.market;

import com.smartcrop.market.client.MarketDataProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Profile("dev")
public class DevelopmentMarketDataProvider implements MarketDataProvider {

    private static final LocalDateTime OBSERVED_AT = LocalDateTime.of(2026, 8, 25, 9, 0);

    @Override
    public List<MarketPriceData> getPrices() {
        return List.of(
                price("Pune APMC", "Pune", "Maharashtra", "Rice", "Common", 2800, 3200, 3050),
                price("Nashik APMC", "Nashik", "Maharashtra", "Rice", "Common", 2900, 3350, 3200),
                price("Pune APMC", "Pune", "Maharashtra", "Wheat", "Lokwan", 2400, 2750, 2600),
                price("Indore Mandi", "Indore", "Madhya Pradesh", "Wheat", "Lokwan", 2500, 2850, 2700),
                price("Pune APMC", "Pune", "Maharashtra", "Tomato", "Hybrid", 1400, 1900, 1650),
                price("Nashik APMC", "Nashik", "Maharashtra", "Tomato", "Hybrid", 1500, 2100, 1850));
    }

    private MarketPriceData price(String marketName, String district, String state,
            String cropName, String variety, int minPrice, int maxPrice, int modalPrice) {
        return new MarketPriceData(
                marketName, district, state, cropName, variety, "quintal",
                BigDecimal.valueOf(minPrice), BigDecimal.valueOf(maxPrice),
                BigDecimal.valueOf(modalPrice), "INR", OBSERVED_AT);
    }
}
