package com.smartcrop.advisory.controller;

import com.smartcrop.advisory.service.AdvisoryService.CropNotFoundException;
import com.smartcrop.advisory.service.AdvisoryService.FarmerProfileNotFoundException;
import com.smartcrop.advisory.service.AdvisoryService.AdvisoryNotFoundException;
import com.smartcrop.weather.client.OpenMeteoClient.MalformedWeatherResponseException;
import com.smartcrop.weather.client.OpenMeteoClient.WeatherProviderUnavailableException;
import com.smartcrop.weather.client.OpenMeteoClient.WeatherTimeoutException;
import com.smartcrop.weather.service.WeatherService.FarmerCoordinatesMissingException;
import com.smartcrop.weather.service.WeatherService.InvalidCoordinatesException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.smartcrop.advisory")
public class AdvisoryExceptionHandler {

    @ExceptionHandler(AdvisoryNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleAdvisoryNotFound() {
        return error(HttpStatus.NOT_FOUND, "Advisory not found");
    }

    @ExceptionHandler(CropNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCropNotFound() {
        return error(HttpStatus.NOT_FOUND, "Crop not found");
    }

    @ExceptionHandler(FarmerProfileNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleFarmerProfileNotFound() {
        return error(HttpStatus.NOT_FOUND, "Farmer profile not found");
    }

    @ExceptionHandler(FarmerCoordinatesMissingException.class)
    public ResponseEntity<Map<String, Object>> handleMissingCoordinates() {
        return error(HttpStatus.BAD_REQUEST, "Farmer coordinates are not configured");
    }

    @ExceptionHandler(InvalidCoordinatesException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCoordinates() {
        return error(HttpStatus.BAD_REQUEST, "Farmer coordinates are invalid");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Request validation failed");
        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(WeatherTimeoutException.class)
    public ResponseEntity<Map<String, Object>> handleWeatherTimeout() {
        return error(HttpStatus.GATEWAY_TIMEOUT, "Weather provider request timed out");
    }

    @ExceptionHandler(WeatherProviderUnavailableException.class)
    public ResponseEntity<Map<String, Object>> handleWeatherUnavailable() {
        return error(HttpStatus.SERVICE_UNAVAILABLE, "Weather provider is unavailable");
    }

    @ExceptionHandler(MalformedWeatherResponseException.class)
    public ResponseEntity<Map<String, Object>> handleMalformedWeather() {
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
