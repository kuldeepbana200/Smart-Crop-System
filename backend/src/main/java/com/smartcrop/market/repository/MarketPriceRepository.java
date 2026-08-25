package com.smartcrop.market.repository;

import com.smartcrop.market.entity.MarketPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MarketPriceRepository extends JpaRepository<MarketPrice, Long> {

    List<MarketPrice> findByCropNameIgnoreCaseOrderByObservedAtDesc(String cropName);

    List<MarketPrice> findByCropNameIgnoreCaseAndMarketIdOrderByObservedAtDesc(
            String cropName, Long marketId);

    List<MarketPrice> findByCropNameIgnoreCaseAndMarketDistrictIgnoreCaseOrderByObservedAtDesc(
            String cropName, String district);

    List<MarketPrice> findByCropNameIgnoreCaseAndMarketStateIgnoreCaseOrderByObservedAtDesc(
            String cropName, String state);

    List<MarketPrice> findByCropNameIgnoreCaseAndMarketDistrictIgnoreCaseAndMarketStateIgnoreCaseOrderByObservedAtDesc(
            String cropName, String district, String state);

    Optional<MarketPrice> findByMarketIdAndCropNameIgnoreCaseAndVarietyAndUnitAndObservedAt(
            Long marketId, String cropName, String variety, String unit,
            java.time.LocalDateTime observedAt);
}
