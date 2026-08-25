package com.smartcrop.market.controller;

import com.smartcrop.market.service.MarketService.InvalidMarketFilterException;
import com.smartcrop.market.service.MarketService.MarketNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.smartcrop.market")
public class MarketExceptionHandler {

    @ExceptionHandler(InvalidMarketFilterException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidFilter() {
        return error(HttpStatus.BAD_REQUEST, "A crop name is required for market comparison");
    }

    @ExceptionHandler(MarketNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleMarketNotFound() {
        return error(HttpStatus.NOT_FOUND, "Market not found");
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", Instant.now(), "status", status.value(),
                "error", status.getReasonPhrase(), "message", message));
    }
}
