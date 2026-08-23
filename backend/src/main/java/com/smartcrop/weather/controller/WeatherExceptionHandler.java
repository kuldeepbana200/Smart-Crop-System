package com.smartcrop.weather.controller;

import com.smartcrop.weather.client.OpenMeteoClient.MalformedWeatherResponseException;
import com.smartcrop.weather.client.OpenMeteoClient.WeatherProviderUnavailableException;
import com.smartcrop.weather.client.OpenMeteoClient.WeatherTimeoutException;
import com.smartcrop.weather.service.WeatherService.FarmerCoordinatesMissingException;
import com.smartcrop.weather.service.WeatherService.FarmerProfileNotFoundException;
import com.smartcrop.weather.service.WeatherService.InvalidCoordinatesException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.smartcrop.weather")
public class WeatherExceptionHandler {

    @ExceptionHandler(FarmerProfileNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleFarmerProfileNotFound() {
        return error(HttpStatus.NOT_FOUND, "Farmer profile not found");
    }

    @ExceptionHandler(FarmerCoordinatesMissingException.class)
    public ResponseEntity<Map<String, Object>> handleMissingCoordinates() {
        return error(HttpStatus.UNPROCESSABLE_ENTITY, "Farmer coordinates are not configured");
    }

    @ExceptionHandler(InvalidCoordinatesException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCoordinates() {
        return error(HttpStatus.BAD_REQUEST, "Farmer coordinates are invalid");
    }

    @ExceptionHandler(WeatherTimeoutException.class)
    public ResponseEntity<Map<String, Object>> handleTimeout() {
        return error(HttpStatus.GATEWAY_TIMEOUT, "Weather provider request timed out");
    }

    @ExceptionHandler(WeatherProviderUnavailableException.class)
    public ResponseEntity<Map<String, Object>> handleProviderUnavailable() {
        return error(HttpStatus.SERVICE_UNAVAILABLE, "Weather provider is unavailable");
    }

    @ExceptionHandler(MalformedWeatherResponseException.class)
    public ResponseEntity<Map<String, Object>> handleMalformedResponse() {
        return error(HttpStatus.BAD_GATEWAY, "Weather provider returned an invalid response");
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", Instant.now(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message));
    }
}
