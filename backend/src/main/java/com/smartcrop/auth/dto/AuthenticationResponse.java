package com.smartcrop.auth.dto;

import com.smartcrop.auth.entity.Role;

public record AuthenticationResponse(
        String token,
        Long userId,
        String name,
        String email,
        Role role) {
}
