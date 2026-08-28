package com.smartcrop.dashboard.controller;

import com.smartcrop.dashboard.dto.DashboardResponse;
import com.smartcrop.dashboard.service.DashboardService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(
            DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    @PreAuthorize("hasRole('FARMER')")
    public DashboardResponse getDashboard(
            Authentication authentication) {

        return dashboardService.getDashboard(authentication);
    }
}