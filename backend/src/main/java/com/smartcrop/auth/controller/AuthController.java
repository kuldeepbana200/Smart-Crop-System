package com.smartcrop.auth.controller;

import com.smartcrop.auth.dto.AuthenticationResponse;
import com.smartcrop.auth.dto.LoginRequest;
import com.smartcrop.auth.dto.RegistrationRequest;
import com.smartcrop.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody RegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public AuthenticationResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/language")
    public ResponseEntity<String> getLanguage(HttpServletRequest request) {
        try {
            // First try to get language from authenticated user's preference
            String language = authService.getPreferredLanguage();
            if (language != null && !language.isEmpty()) {
                return ResponseEntity.ok(language);
            }
        } catch (Exception e) {
            // If there's an error getting authenticated user's preference, fall back to header/param
        }

        // Check for lang query parameter
        String langParam = request.getParameter("lang");
        if (langParam != null) {
            // Validate lang parameter
            if (langParam.equals("en") || langParam.equals("hi") ||
                langParam.equals("or") || langParam.equals("mr")) {
                return ResponseEntity.ok(langParam);
            }
        }

        // Check Accept-Language header
        String acceptLanguage = request.getHeader("Accept-Language");
        if (acceptLanguage != null) {
            // Simplified parsing - take first language tag
            String[] languages = acceptLanguage.split(",");
            for (String lang : languages) {
                String langCode = lang.trim().split(";")[0].trim().toLowerCase();
                if (langCode.equals("en") || langCode.equals("hi") ||
                    langCode.equals("or") || langCode.equals("mr")) {
                    // Handle language variants like en-US, hi-IN, etc.
                    if (langCode.startsWith("en")) {
                        return ResponseEntity.ok("en");
                    } else if (langCode.startsWith("hi")) {
                        return ResponseEntity.ok("hi");
                    } else if (langCode.startsWith("or")) {
                        return ResponseEntity.ok("or");
                    } else if (langCode.startsWith("mr")) {
                        return ResponseEntity.ok("mr");
                    }
                }
            }
        }

        // Default to English
        return ResponseEntity.ok("en");
    }

    @PutMapping("/language")
    public ResponseEntity<Void> setLanguage(@RequestBody String language) {
        try {
            authService.setPreferredLanguage(language);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            // User not authenticated or other error
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    // OTP Verification Endpoints

    @PostMapping("/send-otp")
    public ResponseEntity<Void> sendOTP(@RequestBody String email) {
        boolean sent = authService.resendOTP(email);
        if (sent) {
            return ResponseEntity.ok().build();
        } else {
            // For security, don't reveal whether email exists or not
            return ResponseEntity.accepted().build();
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Void> verifyOTP(@RequestBody String otp) {
        boolean verified = authService.verifyOTP(otp);
        if (verified) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<Void> resendOTP(@RequestBody String email) {
        boolean sent = authService.resendOTP(email);
        if (sent) {
            return ResponseEntity.ok().build();
        } else {
            // For security, don't reveal whether email exists or not
            return ResponseEntity.accepted().build();
        }
    }
}