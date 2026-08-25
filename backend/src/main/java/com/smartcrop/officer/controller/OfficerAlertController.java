package com.smartcrop.officer.controller;

import com.smartcrop.distress.dto.DistressAlertResponse;
import com.smartcrop.distress.service.DistressAlertService;
import com.smartcrop.officer.dto.AssignAlertRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/officer/alerts")
public class OfficerAlertController {

    private final DistressAlertService distressAlertService;

    public OfficerAlertController(DistressAlertService distressAlertService) {
        this.distressAlertService = distressAlertService;
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('OFFICER', 'ADMIN')")
    public DistressAlertResponse assign(
            @PathVariable Long id,
            @Valid @RequestBody AssignAlertRequest request) {
        return distressAlertService.assign(id, request);
    }
}
