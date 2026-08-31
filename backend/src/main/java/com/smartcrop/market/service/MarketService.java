package com.smartcrop.market.service;

import com.smartcrop.farmer.entity.Farmer;
import com.smartcrop.market.dto.MarketPriceResponse;
import com.smartcrop.market.entity.Market;
import com.smartcrop.market.entity.MarketPrice;
import com.smartcrop.market.repository.MarketRepository;
import com.smartcrop.market.repository.MarketPriceRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class MarketService {

        private static final Logger logger = LoggerFactory.getLogger(MarketService.class);

        private final MarketRepository marketRepository;
        private final MarketPriceRepository marketPriceRepository;
        private final GroqMarketDataService groqMarketDataService;

        public MarketService(
                        MarketRepository marketRepository,
                        MarketPriceRepository marketPriceRepository,
                        GroqMarketDataService groqMarketDataService) {

                this.marketRepository = marketRepository;
                this.marketPriceRepository = marketPriceRepository;
                this.groqMarketDataService = groqMarketDataService;
        }

        // =========================================================
        // MARKET OPERATIONS
        // =========================================================

        public Market findOrCreateMarket(
                        String name,
                        String district,
                        String state) {

                return marketRepository
                                .findByNameAndDistrictAndState(name, district, state)
                                .orElseGet(() -> {

                                        Market market = new Market();

                                        market.setName(name);
                                        market.setDistrict(district);
                                        market.setState(state);

                                        return marketRepository.save(market);
                                });
        }

        // =========================================================
        // MARKET PRICE OPERATIONS
        // =========================================================

        public void saveMarketPrice(MarketPrice marketPrice) {
                marketPriceRepository.save(marketPrice);
        }

        // =========================================================
        // GET PRICES
        // =========================================================

        public List<MarketPriceResponse> getPricesForFarmer(
                        Farmer farmer,
                        String cropName,
                        String district,
                        String state) {

                if (farmer != null) {
                        List<MarketPriceResponse> generated = groqMarketDataService.getPricesForFarmer(
                                        farmer,
                                        cropName,
                                        district,
                                        state);
                        if (!generated.isEmpty()) {
                                return generated;
                        }
                }

                return getPrices(cropName, district, state);
        }

        public List<MarketPriceResponse> getPrices(
                        String cropName,
                        String district,
                        String state) {

                List<MarketPrice> prices;

                if (cropName != null && !cropName.isBlank()) {

                        if (state != null && !state.isBlank()) {

                                prices = marketPriceRepository
                                                .findByCropNameAndMarket_State(
                                                                cropName,
                                                                state);

                        } else if (district != null && !district.isBlank()) {

                                prices = marketPriceRepository
                                                .findByCropNameAndMarket_District(
                                                                cropName,
                                                                district);

                        } else {

                                prices = marketPriceRepository
                                                .findByCropName(cropName);
                        }

                } else {

                        if (state != null && !state.isBlank()) {

                                prices = marketPriceRepository
                                                .findByMarket_State(state);

                        } else if (district != null && !district.isBlank()) {

                                prices = marketPriceRepository
                                                .findByMarket_District(district);

                        } else {

                                prices = marketPriceRepository.findAll();
                        }
                }

                return prices.stream()
                                .map(this::mapToResponse)
                                .toList();
        }

        // =========================================================
        // PRICE HISTORY
        // =========================================================

        public List<MarketPriceResponse> getPriceHistoryForFarmer(
                        Farmer farmer,
                        String cropName,
                        String state,
                        LocalDate startDate,
                        LocalDate endDate) {

                if (farmer != null) {
                        List<MarketPriceResponse> generated = groqMarketDataService.getPricesForFarmer(
                                        farmer,
                                        cropName,
                                        farmer.getDistrict(),
                                        farmer.getState());
                        if (!generated.isEmpty()) {
                                return generated.stream()
                                                .filter(price -> price.getArrivalDate() != null)
                                                .filter(price -> !price.getArrivalDate().isBefore(startDate))
                                                .filter(price -> !price.getArrivalDate().isAfter(endDate))
                                                .sorted(Comparator.comparing(MarketPriceResponse::getArrivalDate))
                                                .toList();
                        }
                }

                return getPriceHistory(cropName, state, startDate, endDate);
        }

        public List<MarketPriceResponse> getPriceHistory(
                        String cropName,
                        String state,
                        LocalDate startDate,
                        LocalDate endDate) {

                logger.info(
                                "Entering getPriceHistory: cropName={}, state={}, startDate={}, endDate={}",
                                cropName,
                                state,
                                startDate,
                                endDate);

                List<MarketPrice> prices;

                /*
                 * IMPORTANT:
                 *
                 * Filtering is performed by the DATABASE.
                 *
                 * We do NOT use findAll()
                 * We do NOT fetch all crop prices and filter in Java.
                 */

                if (cropName != null && !cropName.isBlank()) {

                        if (state != null && !state.isBlank()) {

                                prices = marketPriceRepository
                                                .findByCropNameAndMarket_StateAndObservedAtBetween(
                                                                cropName,
                                                                state,
                                                                startDate,
                                                                endDate);

                        } else {

                                prices = marketPriceRepository
                                                .findByCropNameAndObservedAtBetween(
                                                                cropName,
                                                                startDate,
                                                                endDate);
                        }

                } else {

                        if (state != null && !state.isBlank()) {

                                prices = marketPriceRepository
                                                .findByMarket_StateAndObservedAtBetween(
                                                                state,
                                                                startDate,
                                                                endDate);

                        } else {

                                prices = marketPriceRepository
                                                .findByObservedAtBetween(
                                                                startDate,
                                                                endDate);
                        }
                }

                logger.info(
                                "Found {} prices from database before mapping",
                                prices.size());

                List<MarketPriceResponse> result = prices.stream()
                                .sorted(
                                                Comparator.comparing(
                                                                MarketPrice::getObservedAt))
                                .map(this::mapToResponse)
                                .toList();

                logger.info(
                                "Returning {} price history responses",
                                result.size());

                return result;
        }

        // =========================================================
        // PRICE COMPARISON
        // =========================================================

        public List<MarketPriceResponse> getPriceComparisonForFarmer(
                        Farmer farmer,
                        String cropName,
                        String state,
                        String district) {

                if (farmer != null) {
                        List<MarketPriceResponse> generated = groqMarketDataService.getPricesForFarmer(
                                        farmer,
                                        cropName,
                                        district,
                                        state);
                        if (!generated.isEmpty()) {
                                return generated;
                        }
                }

                return getPriceComparison(cropName, state, district);
        }

        public List<MarketPriceResponse> getPriceComparison(
                        String cropName,
                        String state,
                        String district) {

                List<MarketPrice> prices;

                if (state != null && !state.isBlank()) {

                        if (district != null && !district.isBlank()) {

                                prices = marketPriceRepository
                                                .findByCropNameAndMarket_District(
                                                                cropName,
                                                                district);

                        } else {

                                prices = marketPriceRepository
                                                .findByCropNameAndMarket_State(
                                                                cropName,
                                                                state);
                        }

                } else {

                        prices = marketPriceRepository
                                        .findByCropName(cropName);
                }

                return prices.stream()
                                .sorted(
                                                Comparator
                                                                .comparing(MarketPrice::getObservedAt)
                                                                .reversed())
                                .map(this::mapToResponse)
                                .toList();
        }

        // =========================================================
        // LATEST PRICES FOR CROP
        // =========================================================

        public List<MarketPriceResponse> getLatestForCrop(
                        String cropName) {

                if (cropName == null || cropName.isBlank()) {
                        return List.of();
                }

                return marketPriceRepository
                                .findByCropName(cropName)
                                .stream()
                                .map(this::mapToResponse)
                                .toList();
        }

        /**
         * Get latest prices for multiple crops in a single query.
         *
         * @param cropNames list of crop names
         * @return map of crop name to list of market price responses
         */
        public Map<String, List<MarketPriceResponse>> getLatestForCrops(List<String> cropNames) {
                if (cropNames == null || cropNames.isEmpty()) {
                        return Map.of();
                }
                List<MarketPrice> prices = marketPriceRepository.findByCropNameIn(cropNames);
                return prices.stream()
                                .collect(Collectors.groupingBy(
                                                price -> price.getCropName(),
                                                Collectors.mapping(this::mapToResponse, Collectors.toList())));
        }

        // =========================================================
        // ENTITY -> DTO
        // =========================================================

        private MarketPriceResponse mapToResponse(
                        MarketPrice price) {

                Market market = price.getMarket();

                MarketPriceResponse response = new MarketPriceResponse(
                                price.getId(),
                                market.getState(),
                                market.getDistrict(),
                                market.getName(),
                                price.getCropName(),
                                price.getVariety(),
                                price.getGrade(),
                                price.getObservedAt(),
                                price.getMinPrice(),
                                price.getMaxPrice(),
                                price.getModalPrice());
                response.setSource("verified");
                return response;
        }
}