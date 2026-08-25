package com.smartcrop.officer.controller;

import com.smartcrop.distress.service.DistressAlertService.AssignedOfficerNotFoundException;
import com.smartcrop.distress.service.DistressAlertService.InvalidAssignedOfficerException;
import com.smartcrop.distress.service.DistressAlertService.AlertNotFoundException;
import com.smartcrop.distress.service.DistressAlertService.AssignedOfficerConflictException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.smartcrop.officer")
public class OfficerExceptionHandler {

    @ExceptionHandler(AlertNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleAlertNotFound() {
        return error(HttpStatus.NOT_FOUND, "Distress alert not found");
    }

    @ExceptionHandler(AssignedOfficerNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleOfficerNotFound() {
        return error(HttpStatus.NOT_FOUND, "Officer not found");
    }

    @ExceptionHandler(InvalidAssignedOfficerException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidOfficer() {
        return error(HttpStatus.BAD_REQUEST, "Assigned user must have the OFFICER role");
    }

    @ExceptionHandler(AssignedOfficerConflictException.class)
    public ResponseEntity<Map<String, Object>> handleOfficerConflict() {
        return error(HttpStatus.FORBIDDEN, "Alert is assigned to a different officer");
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", Instant.now(), "status", status.value(),
                "error", status.getReasonPhrase(), "message", message));
    }
}
