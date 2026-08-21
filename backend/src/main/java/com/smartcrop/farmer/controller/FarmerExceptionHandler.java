package com.smartcrop.farmer.controller;

import com.smartcrop.farmer.service.FarmerService.FarmerProfileNotFoundException;
import com.smartcrop.farmer.service.FarmerService.DuplicateFarmerProfileException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.smartcrop.farmer")
public class FarmerExceptionHandler {

    @ExceptionHandler(DuplicateFarmerProfileException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateProfile() {
        return error(HttpStatus.CONFLICT, "Farmer profile already exists");
    }

    @ExceptionHandler(FarmerProfileNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleProfileNotFound() {
        return error(HttpStatus.NOT_FOUND, "Farmer profile not found");
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
