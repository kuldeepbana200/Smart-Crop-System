package com.smartcrop.dashboard.controller;

import com.smartcrop.dashboard.service.DashboardService.FarmerProfileNotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.smartcrop.dashboard")
public class DashboardExceptionHandler {

    @ExceptionHandler(FarmerProfileNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleFarmerProfileNotFound() {

        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of(
                        "timestamp", Instant.now(),
                        "status", HttpStatus.UNPROCESSABLE_ENTITY.value(),
                        "error", HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(),
                        "message", "Farmer profile not found"));
    }
}