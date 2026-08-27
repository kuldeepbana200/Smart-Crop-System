package com.smartcrop.market.repository;

import com.smartcrop.market.entity.Market;
import com.smartcrop.market.entity.MarketPrice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MarketPriceRepository extends JpaRepository<MarketPrice, Long> {

    List<MarketPrice> findByMarket_State(String state);

    List<MarketPrice> findByMarket_District(String district);

    List<MarketPrice> findByCropName(String cropName);

    List<MarketPrice> findByCropNameAndMarket_State(
            String cropName,
            String state);

    List<MarketPrice> findByCropNameAndMarket_District(
            String cropName,
            String district);

    List<MarketPrice> findByObservedAtBetween(
            LocalDate startDate,
            LocalDate endDate);

    /*
     * History queries.
     * Filtering is performed by PostgreSQL instead of loading
     * all 10,000 records into Java.
     */
    List<MarketPrice> findByCropNameAndObservedAtBetween(
            String cropName,
            LocalDate startDate,
            LocalDate endDate);

    List<MarketPrice> findByCropNameAndMarket_StateAndObservedAtBetween(
            String cropName,
            String state,
            LocalDate startDate,
            LocalDate endDate);

    List<MarketPrice> findByMarket_StateAndObservedAtBetween(
            String state,
            LocalDate startDate,
            LocalDate endDate);

    /*
     * Used by MarketDataImporter to prevent duplicate records.
     */
    boolean existsByMarketAndCropNameAndVarietyAndUnitAndObservedAt(
            Market market,
            String cropName,
            String variety,
            String unit,
            LocalDate observedAt);
}