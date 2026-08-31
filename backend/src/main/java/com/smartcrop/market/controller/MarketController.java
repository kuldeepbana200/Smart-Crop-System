package com.smartcrop.market.controller;

import com.smartcrop.auth.entity.User;
import com.smartcrop.auth.repository.UserRepository;
import com.smartcrop.farmer.entity.Farmer;
import com.smartcrop.farmer.repository.FarmerRepository;
import com.smartcrop.market.dto.MarketPriceResponse;
import com.smartcrop.market.service.MarketService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/market/prices")
@PreAuthorize("hasRole('FARMER')")
public class MarketController {

    private static final Logger logger = LoggerFactory.getLogger(MarketController.class);

    private final MarketService marketService;
    private final UserRepository userRepository;
    private final FarmerRepository farmerRepository;

    @Autowired
    public MarketController(
            MarketService marketService,
            UserRepository userRepository,
            FarmerRepository farmerRepository) {
        this.marketService = marketService;
        this.userRepository = userRepository;
        this.farmerRepository = farmerRepository;
    }

    /**
     * Get market prices with optional filters.
     *
     * Example:
     * GET /api/market/prices?cropName=Wheat
     * GET /api/market/prices?cropName=Wheat&state=Rajasthan
     */
    @GetMapping
    public ResponseEntity<List<MarketPriceResponse>> getPrices(
            Authentication authentication,
            @RequestParam(required = false) String cropName,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String state) {

        Farmer farmer = resolveFarmer(authentication);
        List<MarketPriceResponse> prices = marketService.getPricesForFarmer(farmer, cropName, district, state);

        return ResponseEntity.ok(prices);
    }

    /**
     * Get historical market prices.
     *
     * Example:
     * GET /api/market/prices/history
     * ?cropName=Wheat
     * &startDate=2026-08-20
     * &endDate=2026-08-27
     *
     * Optional state:
     * GET /api/market/prices/history
     * ?cropName=Wheat
     * &state=Rajasthan
     * &startDate=2026-08-20
     * &endDate=2026-08-27
     */
    @GetMapping("/history")
    public ResponseEntity<List<MarketPriceResponse>> getPriceHistory(
            Authentication authentication,

            @RequestParam(required = false) String cropName,

            @RequestParam(required = false) String state,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        LocalDate today = LocalDate.now();

        // Default end date = today
        if (endDate == null) {
            endDate = today;
        }

        // Default start date = 30 days before today
        if (startDate == null) {
            startDate = today.minusDays(30);
        }

        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().build();
        }

        logger.info(
                "GET /api/market/prices/history - cropName={}, state={}, startDate={}, endDate={}",
                cropName,
                state,
                startDate,
                endDate);

        Farmer farmer = resolveFarmer(authentication);
        List<MarketPriceResponse> history = marketService.getPriceHistoryForFarmer(
                farmer,
                cropName,
                state,
                startDate,
                endDate);

        return ResponseEntity.ok(history);
    }

    /**
     * Compare prices of a crop across markets.
     *
     * Example:
     * GET /api/market/prices/compare?cropName=Wheat
     */
    @GetMapping("/compare")
    public ResponseEntity<List<MarketPriceResponse>> getPriceComparison(
            Authentication authentication,

            @RequestParam String cropName,

            @RequestParam(required = false) String state,

            @RequestParam(required = false) String district) {

        Farmer farmer = resolveFarmer(authentication);
        List<MarketPriceResponse> comparison = marketService.getPriceComparisonForFarmer(
                farmer,
                cropName,
                state,
                district);

        return ResponseEntity.ok(comparison);
    }

    private Farmer resolveFarmer(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return null;
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found"));
        return farmerRepository.findByUserId(user.getId()).orElse(null);
    }
}