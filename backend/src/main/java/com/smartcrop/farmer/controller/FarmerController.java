package com.smartcrop.farmer.controller;

import com.smartcrop.farmer.dto.CreateFarmerProfileRequest;
import com.smartcrop.farmer.dto.FarmerProfileResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.smartcrop.farmer.service.FarmerService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/farmers")
public class FarmerController {

    private final FarmerService farmerService;

    public FarmerController(FarmerService farmerService) {
        this.farmerService = farmerService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('FARMER')")
    public FarmerProfileResponse getMyProfile(Authentication authentication) {
        return farmerService.getMyProfile(authentication);
    }

    @PostMapping("/profile")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<FarmerProfileResponse> createProfile(
            @Valid @RequestBody CreateFarmerProfileRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(farmerService.createProfile(request, authentication));
    }
}
