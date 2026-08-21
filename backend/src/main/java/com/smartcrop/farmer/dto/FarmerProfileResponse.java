package com.smartcrop.farmer.dto;

import com.smartcrop.auth.entity.Role;

public record FarmerProfileResponse(
        Long id,
        String name,
        String email,
        String phone,
        Role role,
        String district,
        String state,
        Double latitude,
        Double longitude,
        Double landArea) {
}
