package com.smartcrop.distress.controller;

import com.smartcrop.distress.service.DistressAlertService.AlertNotFoundException;
import com.smartcrop.distress.service.DistressAlertService.AlertSerializationException;
import com.smartcrop.distress.service.DistressAlertService.FarmerProfileNotFoundException;
import com.smartcrop.distress.service.DistressAlertService.InvalidAlertTransitionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.smartcrop.distress")
public class DistressAlertExceptionHandler {

    @ExceptionHandler(AlertNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound() {
        return error(HttpStatus.NOT_FOUND, "Distress alert not found");
    }

    @ExceptionHandler(FarmerProfileNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleFarmerNotFound() {
        return error(HttpStatus.NOT_FOUND, "Farmer profile not found");
    }

    @ExceptionHandler(InvalidAlertTransitionException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidTransition() {
        return error(HttpStatus.BAD_REQUEST, "Invalid distress alert status transition");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Request validation failed");
        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(AlertSerializationException.class)
    public ResponseEntity<Map<String, Object>> handleSerialization() {
        return error(HttpStatus.BAD_GATEWAY, "Unable to process distress alert data");
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", Instant.now(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message));
    }
}
