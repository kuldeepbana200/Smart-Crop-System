package com.smartcrop.crop.service;

import com.smartcrop.auth.entity.User;
import com.smartcrop.auth.repository.UserRepository;
import com.smartcrop.crop.dto.CreateCropRequest;
import com.smartcrop.crop.dto.CropResponse;
import com.smartcrop.crop.entity.Crop;
import com.smartcrop.crop.repository.CropRepository;
import com.smartcrop.farmer.entity.Farmer;
import com.smartcrop.farmer.repository.FarmerRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CropService {

    private final CropRepository cropRepository;
    private final UserRepository userRepository;
    private final FarmerRepository farmerRepository;

    public CropService(
            CropRepository cropRepository,
            UserRepository userRepository,
            FarmerRepository farmerRepository) {
        this.cropRepository = cropRepository;
        this.userRepository = userRepository;
        this.farmerRepository = farmerRepository;
    }

    @Transactional
    public CropResponse createCrop(CreateCropRequest request, Authentication authentication) {
        if (request.expectedHarvestDate().isBefore(request.sowingDate())) {
            throw new InvalidCropDatesException();
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found"));
        Farmer farmer = farmerRepository.findByUserId(user.getId())
                .orElseThrow(FarmerProfileNotFoundException::new);

        Crop crop = new Crop(
                null,
                farmer,
                request.cropName().trim(),
                normalizeStage(request.cropStage()),
                request.sowingDate(),
                request.expectedHarvestDate(),
                null);

        return toResponse(cropRepository.save(crop));
    }

    @Transactional(readOnly = true)
    public List<CropResponse> getMyCrops(Authentication authentication) {
        Farmer farmer = findAuthenticatedFarmer(authentication);
        return cropRepository.findByFarmerId(farmer.getId()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CropResponse getMyCrop(Long cropId, Authentication authentication) {
        Farmer farmer = findAuthenticatedFarmer(authentication);
        Crop crop = cropRepository.findByIdAndFarmerId(cropId, farmer.getId())
                .orElseThrow(CropNotFoundException::new);
        return toResponse(crop);
    }

    @Transactional
    public CropResponse updateCrop(
            Long cropId,
            CreateCropRequest request,
            Authentication authentication) {
        validateDates(request);

        Farmer farmer = findAuthenticatedFarmer(authentication);
        Crop crop = cropRepository.findByIdAndFarmerId(cropId, farmer.getId())
                .orElseThrow(CropNotFoundException::new);
        crop.updateDetails(
                request.cropName().trim(),
                normalizeStage(request.cropStage()),
                request.sowingDate(),
                request.expectedHarvestDate());

        return toResponse(cropRepository.save(crop));
    }

    @Transactional
    public void deleteCrop(Long cropId, Authentication authentication) {
        Farmer farmer = findAuthenticatedFarmer(authentication);
        Crop crop = cropRepository.findByIdAndFarmerId(cropId, farmer.getId())
                .orElseThrow(CropNotFoundException::new);
        cropRepository.delete(crop);
    }

    private void validateDates(CreateCropRequest request) {
        if (request.expectedHarvestDate().isBefore(request.sowingDate())) {
            throw new InvalidCropDatesException();
        }
    }

    private Farmer findAuthenticatedFarmer(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found"));
        return farmerRepository.findByUserId(user.getId())
                .orElseThrow(FarmerProfileNotFoundException::new);
    }

    private String normalizeStage(String cropStage) {
        return cropStage == null || cropStage.isBlank() ? null : cropStage.trim();
    }

    private CropResponse toResponse(Crop crop) {
        return new CropResponse(
                crop.getId(),
                crop.getFarmer().getId(),
                crop.getCropName(),
                crop.getCropStage(),
                crop.getSowingDate(),
                crop.getExpectedHarvestDate(),
                crop.getCreatedAt());
    }

    public static class FarmerProfileNotFoundException extends RuntimeException {
    }

    public static class InvalidCropDatesException extends RuntimeException {
    }

    public static class CropNotFoundException extends RuntimeException {
    }
}
