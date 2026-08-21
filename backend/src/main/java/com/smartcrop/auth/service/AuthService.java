package com.smartcrop.auth.service;

import com.smartcrop.auth.dto.AuthenticationResponse;
import com.smartcrop.auth.dto.LoginRequest;
import com.smartcrop.auth.dto.RegistrationRequest;
import com.smartcrop.auth.entity.Role;
import com.smartcrop.auth.entity.User;
import com.smartcrop.auth.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthenticationResponse register(RegistrationRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }

        User user = new User(
                null,
                request.name().trim(),
                email,
                normalizePhone(request.phone()),
                passwordEncoder.encode(request.password()),
                Role.FARMER,
                null,
                null);

        User savedUser = userRepository.save(user);
        return createAuthenticationResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthenticationResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(normalizeEmail(request.email()))
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        return createAuthenticationResponse(user);
    }

    private AuthenticationResponse createAuthenticationResponse(User user) {
        return new AuthenticationResponse(
                jwtService.generateToken(user.getEmail()),
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private String normalizePhone(String phone) {
        return phone == null || phone.isBlank() ? null : phone.trim();
    }

    public static class DuplicateEmailException extends RuntimeException {
    }
}
