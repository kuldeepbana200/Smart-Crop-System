package com.smartcrop.market.repository;

import com.smartcrop.market.entity.Market;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarketRepository extends JpaRepository<Market, Long> {

    /**
     * Find a market using its unique combination of:
     * name + district + state.
     */
    Optional<Market> findByNameAndDistrictAndState(
            String name,
            String district,
            String state);

    /**
     * Find all markets in a state.
     */
    List<Market> findByState(String state);

    /**
     * Find all markets in a district.
     */
    List<Market> findByDistrict(String district);
}