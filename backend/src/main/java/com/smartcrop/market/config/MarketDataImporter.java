package com.smartcrop.market.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcrop.market.entity.Market;
import com.smartcrop.market.entity.MarketPrice;
import com.smartcrop.market.repository.MarketPriceRepository;
import com.smartcrop.market.repository.MarketRepository;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
public class MarketDataImporter {

        private final MarketRepository marketRepository;
        private final MarketPriceRepository marketPriceRepository;

        @Value("${app.market.provider:groq}")
        private String marketProvider;

        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        public MarketDataImporter(
                        MarketRepository marketRepository,
                        MarketPriceRepository marketPriceRepository) {

                this.marketRepository = marketRepository;
                this.marketPriceRepository = marketPriceRepository;
        }

        @PostConstruct
        @Transactional
        public void importData() {

                if (!"json".equalsIgnoreCase(marketProvider)) {
                        System.out.println("Market provider is '" + marketProvider
                                        + "'. Skipping legacy hardcoded market JSON import.");
                        return;
                }

                /*
                 * IMPORTANT:
                 *
                 * Market data is imported only once.
                 *
                 * If market_price already contains records,
                 * the JSON file is NOT loaded again.
                 */
                long existingRecords = marketPriceRepository.count();

                if (existingRecords > 0) {
                        System.out.println(
                                        "Market data already exists (" +
                                                        existingRecords +
                                                        " records). Skipping JSON import.");
                        return;
                }

                System.out.println(
                                "No market data found. Starting initial JSON import...");

                try {

                        ObjectMapper objectMapper = new ObjectMapper();

                        ClassPathResource resource = new ClassPathResource("market_data.json");

                        Map<String, Object> jsonMap = objectMapper.readValue(
                                        resource.getInputStream(),
                                        Map.class);

                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> records = (List<Map<String, Object>>) jsonMap.get("records");

                        if (records == null || records.isEmpty()) {
                                System.out.println(
                                                "market_data.json contains no records.");
                                return;
                        }

                        System.out.println(
                                        "Starting import of " +
                                                        records.size() +
                                                        " market price records...");

                        int importedCount = 0;

                        for (Map<String, Object> record : records) {

                                String state = getString(record, "state");

                                String district = getString(record, "district");

                                String marketName = getString(record, "market");

                                String commodity = getString(record, "commodity");

                                String variety = getString(record, "variety");

                                String grade = getString(record, "grade");

                                String arrivalDateStr = getString(record, "arrival_date");

                                Double minPrice = getDouble(record, "min_price");

                                Double maxPrice = getDouble(record, "max_price");

                                Double modalPrice = getDouble(record, "modal_price");

                                LocalDate observedAt = LocalDate.parse(
                                                arrivalDateStr,
                                                DATE_FORMATTER);

                                /*
                                 * Find existing market.
                                 * Create it only if it does not exist.
                                 */
                                Market market = marketRepository
                                                .findByNameAndDistrictAndState(
                                                                marketName,
                                                                district,
                                                                state)
                                                .orElseGet(() -> {

                                                        Market newMarket = new Market();

                                                        newMarket.setName(marketName);
                                                        newMarket.setDistrict(district);
                                                        newMarket.setState(state);

                                                        return marketRepository.save(
                                                                        newMarket);
                                                });

                                /*
                                 * Database is empty at this point,
                                 * so we don't perform an EXISTS query
                                 * for every single record.
                                 */
                                MarketPrice marketPrice = new MarketPrice();

                                marketPrice.setMarket(market);
                                marketPrice.setCropName(commodity);
                                marketPrice.setVariety(variety);
                                marketPrice.setGrade(grade);
                                marketPrice.setObservedAt(observedAt);

                                marketPrice.setMinPrice(minPrice);
                                marketPrice.setMaxPrice(maxPrice);
                                marketPrice.setModalPrice(modalPrice);

                                marketPrice.setCurrency("INR");
                                marketPrice.setUnit("quintal");

                                marketPriceRepository.save(marketPrice);

                                importedCount++;

                                if (importedCount % 500 == 0) {
                                        System.out.println(
                                                        "Imported " +
                                                                        importedCount +
                                                                        "/" +
                                                                        records.size() +
                                                                        " market records...");
                                }
                        }

                        System.out.println(
                                        "==============================================");

                        System.out.println(
                                        "Market data import completed successfully.");

                        System.out.println(
                                        "Imported records: " +
                                                        importedCount);

                        System.out.println(
                                        "==============================================");

                } catch (IOException e) {

                        System.err.println(
                                        "Failed to import market data: " +
                                                        e.getMessage());

                        throw new IllegalStateException(
                                        "Market data import failed. " +
                                                        "Check market_data.json.",
                                        e);
                }
        }

        private String getString(
                        Map<String, Object> record,
                        String field) {

                Object value = record.get(field);

                if (value == null) {
                        throw new IllegalArgumentException(
                                        "Missing required market field: " +
                                                        field);
                }

                return value.toString().trim();
        }

        private Double getDouble(
                        Map<String, Object> record,
                        String field) {

                Object value = record.get(field);

                if (value == null) {
                        throw new IllegalArgumentException(
                                        "Missing required market field: " +
                                                        field);
                }

                if (value instanceof Number number) {
                        return number.doubleValue();
                }

                try {
                        return Double.parseDouble(
                                        value.toString());
                } catch (NumberFormatException e) {
                        throw new IllegalArgumentException(
                                        "Invalid numeric value for field " +
                                                        field +
                                                        ": " +
                                                        value,
                                        e);
                }
        }
}