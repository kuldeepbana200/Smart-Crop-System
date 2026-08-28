package com.smartcrop.advisory.controller;

import com.smartcrop.advisory.dto.AdvisoryResponse;
import com.smartcrop.advisory.dto.GenerateAdvisoryRequest;
import com.smartcrop.advisory.service.AdvisoryService;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/advisories")
public class AdvisoryController {

    private final AdvisoryService advisoryService;

    public AdvisoryController(
            AdvisoryService advisoryService) {

        this.advisoryService = advisoryService;
    }

    @PostMapping("/generate")
    @PreAuthorize("hasRole('FARMER')")
    public AdvisoryResponse generateAdvisory(
            @Valid @RequestBody GenerateAdvisoryRequest request,
            Authentication authentication) {

        return advisoryService.generateAdvisory(
                request,
                authentication);
    }

    @GetMapping
    @PreAuthorize("hasRole('FARMER')")
    public List<AdvisoryResponse> getMyAdvisories(
            Authentication authentication,
            @RequestParam(required = false) String language) {

        return advisoryService.getMyAdvisories(
                authentication,
                language);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('FARMER')")
    public AdvisoryResponse getMyAdvisory(
            @PathVariable Long id,
            Authentication authentication,
            @RequestParam(required = false) String language) {

        return advisoryService.getMyAdvisory(
                id,
                authentication,
                language);
    }
}