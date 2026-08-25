package com.smartcrop.intervention.controller;

import com.smartcrop.intervention.dto.CreateInterventionRequest;
import com.smartcrop.intervention.dto.InterventionResponse;
import com.smartcrop.intervention.dto.UpdateInterventionRequest;
import com.smartcrop.intervention.service.InterventionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/officer")
public class InterventionController {

    private final InterventionService interventionService;

    public InterventionController(InterventionService interventionService) {
        this.interventionService = interventionService;
    }

    @PostMapping("/alerts/{alertId}/interventions")
    @PreAuthorize("hasAnyRole('OFFICER', 'ADMIN')")
    public ResponseEntity<InterventionResponse> create(
            @PathVariable Long alertId,
            @Valid @RequestBody CreateInterventionRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(interventionService.create(alertId, request, authentication));
    }

    @GetMapping("/alerts/{alertId}/interventions")
    @PreAuthorize("hasAnyRole('OFFICER', 'ADMIN')")
    public List<InterventionResponse> getForAlert(
            @PathVariable Long alertId,
            Authentication authentication) {
        return interventionService.getForAlert(alertId, authentication);
    }

    @GetMapping("/interventions/{id}")
    @PreAuthorize("hasAnyRole('OFFICER', 'ADMIN')")
    public InterventionResponse getById(
            @PathVariable Long id,
            Authentication authentication) {
        return interventionService.getById(id, authentication);
    }

    @PatchMapping("/interventions/{id}")
    @PreAuthorize("hasAnyRole('OFFICER', 'ADMIN')")
    public InterventionResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInterventionRequest request,
            Authentication authentication) {
        return interventionService.update(id, request, authentication);
    }
}
