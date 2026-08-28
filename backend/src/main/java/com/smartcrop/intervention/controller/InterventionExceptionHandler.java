package com.smartcrop.intervention.controller;

import com.smartcrop.intervention.service.InterventionService.DistressAlertNotFoundException;
import com.smartcrop.intervention.service.InterventionService.InterventionNotFoundException;
import com.smartcrop.intervention.service.InterventionService.InvalidInterventionTransitionException;
import com.smartcrop.intervention.service.InterventionService.OfficerAccessDeniedException;
import com.smartcrop.intervention.service.InterventionService.DistressAlertAlreadyResolvedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.smartcrop.intervention")
public class InterventionExceptionHandler {

    @ExceptionHandler(DistressAlertNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleAlertNotFound() {
        return error(HttpStatus.NOT_FOUND, "Distress alert not found");
    }

    @ExceptionHandler(InterventionNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleInterventionNotFound() {
        return error(HttpStatus.NOT_FOUND, "Intervention not found");
    }

    @ExceptionHandler(InvalidInterventionTransitionException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidTransition() {
        return error(HttpStatus.BAD_REQUEST, "Invalid intervention status transition");
    }

    @ExceptionHandler(OfficerAccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleOfficerAccessDenied() {
        return error(HttpStatus.FORBIDDEN, "Only officers and administrators may manage interventions");
    }

    @ExceptionHandler(DistressAlertAlreadyResolvedException.class)
    public ResponseEntity<Map<String, Object>> handleAlertAlreadyResolved() {
        return error(HttpStatus.BAD_REQUEST, "Cannot create intervention for a resolved distress alert");
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
