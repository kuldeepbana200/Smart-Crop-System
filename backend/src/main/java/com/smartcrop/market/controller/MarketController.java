package com.smartcrop.market.controller;

import com.smartcrop.market.dto.MarketPriceResponse;
import com.smartcrop.market.service.MarketService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/market/prices")
@PreAuthorize("hasRole('FARMER')")
public class MarketController {

    private final MarketService marketService;

    public MarketController(MarketService marketService) {
        this.marketService = marketService;
    }

    @GetMapping
    public List<MarketPriceResponse> getLatestPrices(
            @RequestParam(required = false) String cropName,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String state) {
        return marketService.getLatestPrices(cropName, district, state);
    }

    @GetMapping("/history")
    public List<MarketPriceResponse> getHistory(
            @RequestParam(required = false) String cropName,
            @RequestParam(required = false) Long marketId,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String state) {
        return marketService.getHistory(cropName, marketId, district, state);
    }

    @GetMapping("/compare")
    public List<MarketPriceResponse> compare(
            @RequestParam String cropName,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String state) {
        return marketService.compare(cropName, district, state);
    }
}
