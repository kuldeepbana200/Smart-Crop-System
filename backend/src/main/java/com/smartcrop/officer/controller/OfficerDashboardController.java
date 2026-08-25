package com.smartcrop.officer.controller;

import com.smartcrop.officer.dto.OfficerDashboardResponse;
import com.smartcrop.officer.service.OfficerDashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/officer/dashboard")
public class OfficerDashboardController {

    private final OfficerDashboardService dashboardService;

    public OfficerDashboardController(OfficerDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OFFICER', 'ADMIN')")
    public OfficerDashboardResponse getDashboard() {
        return dashboardService.getDashboard();
    }
}
