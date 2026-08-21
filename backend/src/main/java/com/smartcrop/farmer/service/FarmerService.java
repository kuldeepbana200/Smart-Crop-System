package com.smartcrop.farmer.service;

import com.smartcrop.auth.entity.User;
import com.smartcrop.auth.repository.UserRepository;
import com.smartcrop.farmer.dto.FarmerProfileResponse;
import com.smartcrop.farmer.dto.CreateFarmerProfileRequest;
import com.smartcrop.farmer.entity.Farmer;
import com.smartcrop.farmer.repository.FarmerRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FarmerService {

    private final FarmerRepository farmerRepository;
    private final UserRepository userRepository;

    public FarmerService(FarmerRepository farmerRepository, UserRepository userRepository) {
        this.farmerRepository = farmerRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public FarmerProfileResponse getMyProfile(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found"));

        Farmer farmer = farmerRepository.findByUserId(user.getId())
                .orElseThrow(FarmerProfileNotFoundException::new);

        return new FarmerProfileResponse(
                farmer.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                farmer.getDistrict(),
                farmer.getState(),
                farmer.getLatitude(),
                farmer.getLongitude(),
                farmer.getLandArea());
    }

    @Transactional
    public FarmerProfileResponse createProfile(
            CreateFarmerProfileRequest request,
            Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found"));

        if (farmerRepository.existsByUserId(user.getId())) {
            throw new DuplicateFarmerProfileException();
        }

        Farmer farmer = new Farmer(
                null,
                user,
                request.district().trim(),
                request.state().trim(),
                request.latitude(),
                request.longitude(),
                request.landArea());

        return toProfileResponse(farmerRepository.save(farmer), user);
    }

    private FarmerProfileResponse toProfileResponse(Farmer farmer, User user) {
        return new FarmerProfileResponse(
                farmer.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                farmer.getDistrict(),
                farmer.getState(),
                farmer.getLatitude(),
                farmer.getLongitude(),
                farmer.getLandArea());
    }

    public static class FarmerProfileNotFoundException extends RuntimeException {
    }

    public static class DuplicateFarmerProfileException extends RuntimeException {
    }
}
