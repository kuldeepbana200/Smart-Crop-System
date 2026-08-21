package com.smartcrop.crop.controller;

import com.smartcrop.crop.dto.CreateCropRequest;
import com.smartcrop.crop.dto.CropResponse;
import com.smartcrop.crop.service.CropService;
import com.smartcrop.crop.service.CropService.CropNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/crops")
public class CropController {

    private final CropService cropService;

    public CropController(CropService cropService) {
        this.cropService = cropService;
    }

    @PostMapping
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<CropResponse> createCrop(
            @Valid @RequestBody CreateCropRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cropService.createCrop(request, authentication));
    }

    @GetMapping
    @PreAuthorize("hasRole('FARMER')")
    public java.util.List<CropResponse> getMyCrops(Authentication authentication) {
        return cropService.getMyCrops(authentication);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('FARMER')")
    public CropResponse getMyCrop(
            @PathVariable Long id,
            Authentication authentication) {
        return cropService.getMyCrop(id, authentication);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FARMER')")
    public CropResponse updateCrop(
            @PathVariable Long id,
            @Valid @RequestBody CreateCropRequest request,
            Authentication authentication) {
        return cropService.updateCrop(id, request, authentication);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<Void> deleteCrop(
            @PathVariable Long id,
            Authentication authentication) {
        cropService.deleteCrop(id, authentication);
        return ResponseEntity.noContent().build();
    }
}
