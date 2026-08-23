package com.smartcrop.distress.controller;

import com.smartcrop.distress.dto.AcknowledgeAlertRequest;
import com.smartcrop.distress.dto.DistressAlertResponse;
import com.smartcrop.distress.dto.ResolveAlertRequest;
import com.smartcrop.distress.entity.AlertStatus;
import com.smartcrop.distress.service.DistressAlertService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DistressAlertController {

    private final DistressAlertService alertService;

    public DistressAlertController(DistressAlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping("/farmers/me/alerts")
    @PreAuthorize("hasRole('FARMER')")
    public List<DistressAlertResponse> getFarmerAlerts(Authentication authentication) {
        return alertService.getFarmerAlerts(authentication);
    }

    @GetMapping("/farmers/me/alerts/{id}")
    @PreAuthorize("hasRole('FARMER')")
    public DistressAlertResponse getFarmerAlert(
            @PathVariable Long id, Authentication authentication) {
        return alertService.getFarmerAlert(id, authentication);
    }

    @GetMapping("/officer/alerts")
    @PreAuthorize("hasAnyRole('OFFICER', 'ADMIN')")
    public List<DistressAlertResponse> getOfficerAlerts(
            @RequestParam(required = false) AlertStatus status) {
        return alertService.getOfficerAlerts(status);
    }

    @GetMapping("/officer/alerts/{id}")
    @PreAuthorize("hasAnyRole('OFFICER', 'ADMIN')")
    public DistressAlertResponse getOfficerAlert(@PathVariable Long id) {
        return alertService.getOfficerAlert(id);
    }

    @PatchMapping("/officer/alerts/{id}/acknowledge")
    @PreAuthorize("hasAnyRole('OFFICER', 'ADMIN')")
    public DistressAlertResponse acknowledge(
            @PathVariable Long id,
            @Valid @RequestBody AcknowledgeAlertRequest request,
            Authentication authentication) {
        return alertService.acknowledge(id, request, authentication);
    }

    @PatchMapping("/officer/alerts/{id}/resolve")
    @PreAuthorize("hasAnyRole('OFFICER', 'ADMIN')")
    public DistressAlertResponse resolve(
            @PathVariable Long id,
            @Valid @RequestBody ResolveAlertRequest request,
            Authentication authentication) {
        return alertService.resolve(id, request, authentication);
    }
}
