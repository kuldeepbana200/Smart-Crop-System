package com.smartcrop.market.repository;

import com.smartcrop.market.entity.Market;
import com.smartcrop.market.entity.MarketPrice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MarketPriceRepository extends JpaRepository<MarketPrice, Long> {

        List<MarketPrice> findByMarket_State(String state);

        List<MarketPrice> findByMarket_District(String district);

        List<MarketPrice> findByCropName(String cropName);

        @Query("select price from MarketPrice price where lower(trim(price.cropName)) = lower(trim(:cropName))")
        List<MarketPrice> findByCropNameIgnoreCase(@Param("cropName") String cropName);

        List<MarketPrice> findByCropNameAndMarket_State(
                        String cropName,
                        String state);

        @Query("select price from MarketPrice price join price.market market "
                        + "where lower(price.cropName) = lower(:cropName) "
                        + "and lower(market.state) = lower(:state) "
                        + "and lower(market.district) = lower(:district)")
        List<MarketPrice> findByCropNameAndMarket_StateAndMarket_DistrictIgnoreCase(
                        @Param("cropName") String cropName,
                        @Param("state") String state,
                        @Param("district") String district);

        @Query("select price from MarketPrice price join price.market market "
                        + "where lower(price.cropName) = lower(:cropName) "
                        + "and lower(market.state) = lower(:state)")
        List<MarketPrice> findByCropNameAndMarket_StateIgnoreCase(
                        @Param("cropName") String cropName,
                        @Param("state") String state);

        @Query("select price from MarketPrice price join price.market market "
                        + "where lower(trim(price.cropName)) = lower(trim(:cropName)) "
                        + "and lower(trim(market.state)) = lower(trim(:state)) "
                        + "and price.observedAt between :startDate and :endDate")
        List<MarketPrice> findByCropNameAndMarket_StateIgnoreCaseAndObservedAtBetween(
                        @Param("cropName") String cropName,
                        @Param("state") String state,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        @Query("select price from MarketPrice price where lower(trim(price.cropName)) = lower(trim(:cropName)) "
                        + "and price.observedAt between :startDate and :endDate")
        List<MarketPrice> findByCropNameIgnoreCaseAndObservedAtBetween(
                        @Param("cropName") String cropName,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

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

        List<MarketPrice> findByCropNameIn(List<String> cropNames);
}