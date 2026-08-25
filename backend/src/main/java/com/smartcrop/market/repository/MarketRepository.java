package com.smartcrop.market.repository;

import com.smartcrop.market.entity.Market;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MarketRepository extends JpaRepository<Market, Long> {

    List<Market> findByStateIgnoreCase(String state);

    List<Market> findByDistrictIgnoreCase(String district);

    Optional<Market> findByNameIgnoreCase(String name);

    Optional<Market> findByNameIgnoreCaseAndDistrictIgnoreCaseAndStateIgnoreCase(
            String name, String district, String state);
}
