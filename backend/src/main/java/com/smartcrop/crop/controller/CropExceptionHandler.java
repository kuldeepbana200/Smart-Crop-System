package com.smartcrop.crop.controller;

import com.smartcrop.crop.service.CropService.FarmerProfileNotFoundException;
import com.smartcrop.crop.service.CropService.InvalidCropDatesException;
import com.smartcrop.crop.service.CropService.CropNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.smartcrop.crop")
public class CropExceptionHandler {

    @ExceptionHandler(CropNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCropNotFound() {
        return error(HttpStatus.NOT_FOUND, "Crop not found");
    }

    @ExceptionHandler(FarmerProfileNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleFarmerProfileNotFound() {
        return error(HttpStatus.NOT_FOUND, "Farmer profile not found");
    }

    @ExceptionHandler(InvalidCropDatesException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidDates() {
        return error(HttpStatus.BAD_REQUEST, "Expected harvest date must not be before sowing date");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Request validation failed");
        return error(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", Instant.now(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message));
    }
}
