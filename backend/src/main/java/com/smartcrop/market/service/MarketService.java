package com.smartcrop.market.service;

import com.smartcrop.market.client.MarketDataProvider;
import com.smartcrop.market.client.MarketDataProvider.MarketPriceData;
import com.smartcrop.market.dto.MarketPriceResponse;
import com.smartcrop.market.entity.Market;
import com.smartcrop.market.entity.MarketPrice;
import com.smartcrop.market.repository.MarketPriceRepository;
import com.smartcrop.market.repository.MarketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class MarketService {

    private final MarketRepository marketRepository;
    private final MarketPriceRepository marketPriceRepository;
    private final MarketDataProvider marketDataProvider;

    public MarketService(
            MarketRepository marketRepository,
            MarketPriceRepository marketPriceRepository,
            MarketDataProvider marketDataProvider) {
        this.marketRepository = marketRepository;
        this.marketPriceRepository = marketPriceRepository;
        this.marketDataProvider = marketDataProvider;
    }

    @Transactional
    public List<MarketPriceResponse> getLatestPrices(String cropName, String district, String state) {
        refreshProviderData();
        String normalizedCrop = normalizeOptional(cropName);
        String normalizedDistrict = normalizeOptional(district);
        String normalizedState = normalizeOptional(state);
        List<MarketPrice> prices = findPrices(normalizedCrop, normalizedDistrict, normalizedState);
        return prices.stream().map(this::toResponse).toList();
    }

    @Transactional
    public List<MarketPriceResponse> getHistory(
            String cropName, Long marketId, String district, String state) {
        refreshProviderData();
        if (marketId != null && !marketRepository.existsById(marketId)) {
            throw new MarketNotFoundException();
        }
        String normalizedCrop = normalizeOptional(cropName);
        String normalizedDistrict = normalizeOptional(district);
        String normalizedState = normalizeOptional(state);
        List<MarketPrice> prices;
        if (marketId != null && normalizedCrop != null) {
            prices = marketPriceRepository.findByCropNameIgnoreCaseAndMarketIdOrderByObservedAtDesc(
                    normalizedCrop, marketId);
        } else if (normalizedCrop != null && normalizedDistrict != null && normalizedState != null) {
            prices = marketPriceRepository
                    .findByCropNameIgnoreCaseAndMarketDistrictIgnoreCaseAndMarketStateIgnoreCaseOrderByObservedAtDesc(
                            normalizedCrop, normalizedDistrict, normalizedState);
        } else if (normalizedCrop != null && normalizedDistrict != null) {
            prices = marketPriceRepository.findByCropNameIgnoreCaseAndMarketDistrictIgnoreCaseOrderByObservedAtDesc(
                    normalizedCrop, normalizedDistrict);
        } else if (normalizedCrop != null && normalizedState != null) {
            prices = marketPriceRepository.findByCropNameIgnoreCaseAndMarketStateIgnoreCaseOrderByObservedAtDesc(
                    normalizedCrop, normalizedState);
        } else if (normalizedCrop != null) {
            prices = marketPriceRepository.findByCropNameIgnoreCaseOrderByObservedAtDesc(normalizedCrop);
        } else {
            prices = marketPriceRepository.findAll().stream()
                    .sorted(Comparator.comparing(MarketPrice::getObservedAt).reversed())
                    .toList();
        }
        return prices.stream()
                .filter(price -> matchesLocation(price, normalizedDistrict, normalizedState))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public List<MarketPriceResponse> compare(String cropName, String district, String state) {
        if (normalizeOptional(cropName) == null) {
            throw new InvalidMarketFilterException();
        }
        return getLatestPrices(cropName, district, state).stream()
                .sorted(Comparator.comparing(MarketPriceResponse::modalPrice).reversed())
                .toList();
    }

    private List<MarketPrice> findPrices(String cropName, String district, String state) {
        if (cropName == null && district == null && state == null) {
            return marketPriceRepository.findAll().stream()
                    .sorted(Comparator.comparing(MarketPrice::getObservedAt).reversed()).toList();
        }
        return marketPriceRepository.findAll().stream()
                .filter(price -> cropName == null || price.getCropName().equalsIgnoreCase(cropName))
                .filter(price -> matchesLocation(price, district, state))
                .sorted(Comparator.comparing(MarketPrice::getObservedAt).reversed())
                .toList();
    }

    private boolean matchesLocation(MarketPrice price, String district, String state) {
        return (district == null || price.getMarket().getDistrict().equalsIgnoreCase(district))
                && (state == null || price.getMarket().getState().equalsIgnoreCase(state));
    }

    private void refreshProviderData() {
        for (MarketPriceData data : marketDataProvider.getPrices()) {
            Market market = marketRepository.findByNameIgnoreCaseAndDistrictIgnoreCaseAndStateIgnoreCase(
                    data.marketName(), data.district(), data.state())
                    .orElseGet(() -> marketRepository.save(new Market(
                            null, data.marketName(), data.district(), data.state(), null)));
            if (marketPriceRepository.findByMarketIdAndCropNameIgnoreCaseAndVarietyAndUnitAndObservedAt(
                    market.getId(), data.cropName(), data.variety(), data.unit(), data.observedAt()).isEmpty()) {
                marketPriceRepository.save(new MarketPrice(
                        null, market, data.cropName().trim(), data.variety().trim(), data.unit().trim(),
                        data.minPrice(), data.maxPrice(), data.modalPrice(), data.currency().trim(),
                        data.observedAt(), null));
            }
        }
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private MarketPriceResponse toResponse(MarketPrice price) {
        return new MarketPriceResponse(
                price.getId(), price.getMarket().getId(), price.getMarket().getName(),
                price.getMarket().getDistrict(), price.getMarket().getState(), price.getCropName(),
                price.getVariety(), price.getUnit(), price.getMinPrice(), price.getMaxPrice(),
                price.getModalPrice(), price.getCurrency(), price.getObservedAt());
    }

    public static class InvalidMarketFilterException extends RuntimeException {
    }

    public static class MarketNotFoundException extends RuntimeException {
    }
}
