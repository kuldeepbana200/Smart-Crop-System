package com.smartcrop.auth.service;

import com.smartcrop.auth.dto.AuthenticationResponse;
import com.smartcrop.auth.dto.LoginRequest;
import com.smartcrop.auth.dto.RegistrationRequest;
import com.smartcrop.auth.entity.OTPVerificationToken;
import com.smartcrop.auth.entity.Role;
import com.smartcrop.auth.entity.User;
import com.smartcrop.auth.repository.OTPVerificationTokenRepository;
import com.smartcrop.auth.repository.UserRepository;
import com.smartcrop.farmer.entity.Farmer;
import com.smartcrop.farmer.repository.FarmerRepository;
import com.smartcrop.auth.service.JwtService;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final FarmerRepository farmerRepository;
    private final OTPVerificationTokenRepository otpVerificationTokenRepository;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            FarmerRepository farmerRepository,
            OTPVerificationTokenRepository otpVerificationTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.farmerRepository = farmerRepository;
        this.otpVerificationTokenRepository = otpVerificationTokenRepository;
    }

    @Transactional
    public AuthenticationResponse register(RegistrationRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }

        // Determine language: use provided language if valid, otherwise default to "en"
        String language = "en"; // default
        if (request.language() != null &&
                (request.language().equals("en") || request.language().equals("hi") ||
                        request.language().equals("or") || request.language().equals("mr"))) {
            language = request.language();
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

        // Set the preferred language and verification status
        user.setPreferredLanguage(language);
        user.setPhoneVerified(false); // Phone verification required after registration

        User savedUser = userRepository.save(user);

        // Create the linked Farmer row immediately so the user always has a valid
        // farm record, but keep farm setup details nullable until the farmer
        // completes their onboarding information.
        if (!farmerRepository.existsByUserId(savedUser.getId())) {
            Farmer farmer = new Farmer();
            farmer.setUser(savedUser);
            farmer.setDistrict(null);
            farmer.setState(null);
            farmer.setLatitude(null);
            farmer.setLongitude(null);
            farmer.setLandArea(null);
            farmerRepository.save(farmer);
        }

        // Generate and send OTP for phone verification
        String otp = generateAndSendOTP(savedUser);

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

    /**
     * Get the preferred language of the currently authenticated user.
     * Defaults to "en" if user is not authenticated or language is not set.
     */
    @Transactional(readOnly = true)
    public String getPreferredLanguage() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "en"; // default fallback
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found: " + email));

        String language = user.getPreferredLanguage();
        return (language != null && !language.isEmpty()) ? language : "en";
    }

    /**
     * Set the preferred language for the currently authenticated user.
     * 
     * @param language Language code (en, hi, or, mr)
     * @throws IllegalArgumentException if language is not supported
     */
    @Transactional
    public void setPreferredLanguage(String language) {
        // Validate language code
        if (language == null || (!language.equals("en") && !language.equals("hi") &&
                !language.equals("or") && !language.equals("mr"))) {
            throw new IllegalArgumentException("Unsupported language: " + language);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found: " + email));

        user.setPreferredLanguage(language);
        userRepository.save(user);
    }

    private AuthenticationResponse createAuthenticationResponse(User user) {
        return new AuthenticationResponse(
                jwtService.generateToken(user.getEmail()),
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getPreferredLanguage(),
                user.isPhoneVerified());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private String normalizePhone(String phone) {
        return phone == null || phone.isBlank() ? null : phone.trim();
    }

    public static class DuplicateEmailException extends RuntimeException {
    }

    /**
     * Generate and send OTP for phone verification.
     * 
     * @param user The user to generate OTP for
     * @return The generated OTP code
     */
    @Transactional
    public String generateAndSendOTP(User user) {
        // Generate a 6-digit OTP
        String otp = generateOTP();

        // Set expiry to 5 minutes from now
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(5);

        // Create and save the OTP token
        OTPVerificationToken otpToken = new OTPVerificationToken(otp, user.getId(), expiryDate);
        otpVerificationTokenRepository.save(otpToken);

        // Send OTP via SMS
        sendOTPViaSMS(user.getPhone(), otp);

        return otp;
    }

    /**
     * Generate a 6-digit OTP code.
     * 
     * @return A 6-digit OTP code
     */
    private String generateOTP() {
        Random random = new Random();
        int otpValue = 100000 + random.nextInt(900000); // Generates a number between 100000 and 999999
        return String.valueOf(otpValue);
    }

    /**
     * Send OTP via SMS.
     * In a real implementation, this would integrate with an SMS service (Twilio,
     * etc.)
     * For now, we'll just log the OTP.
     * 
     * @param phoneNumber The user's phone number
     * @param otp         The OTP code to send
     */
    @Transactional
    public void sendOTPViaSMS(String phoneNumber, String otp) {
        // In a real implementation, send SMS via SMS service
        // For now, just log it (in production, use proper logging framework)
        System.out.println("OTP for phone " + phoneNumber + ": " + otp);
        // TODO: Integrate with actual SMS service
    }

    /**
     * Verify OTP using the provided code.
     * 
     * @param otp The OTP code to verify
     * @return true if verification successful, false otherwise
     */
    @Transactional
    public boolean verifyOTP(String otp) {
        OTPVerificationToken otpToken = otpVerificationTokenRepository.findByOtp(otp)
                .orElse(null);

        if (otpToken == null) {
            return false; // OTP not found
        }

        // Check if OTP has expired
        if (otpToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return false; // OTP expired
        }

        // Check if already verified
        if (otpToken.isVerified()) {
            return true; // Already verified
        }

        // Mark OTP as verified
        otpToken.setVerified(true);
        otpVerificationTokenRepository.save(otpToken);

        // Update user's phoneVerified flag
        User user = userRepository.findById(otpToken.getUserId())
                .orElseThrow(() -> new IllegalStateException("User not found for OTP token"));
        user.setPhoneVerified(true);
        userRepository.save(user);

        return true;
    }

    /**
     * Resend OTP for the given email address.
     * 
     * @param email The user's email address
     * @return true if resent successfully, false if user not found
     */
    @Transactional
    public boolean resendOTP(String email) {
        User user = userRepository.findByEmail(normalizeEmail(email))
                .orElse(null);

        if (user == null) {
            return false; // User not found
        }

        if (user.isPhoneVerified()) {
            return true; // Already verified, nothing to do
        }

        // Generate new OTP and send it
        String otp = generateAndSendOTP(user);

        return true;
    }

    /**
     * Get the preferred language of the currently authenticated user.
     * Defaults to "en" if user is not authenticated or language is not set.
     * This method is kept for backward compatibility with existing code.
     */
    @Transactional(readOnly = true)
    public String getPreferredLanguageFromContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "en"; // default fallback
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found: " + email));

        String language = user.getPreferredLanguage();
        return (language != null && !language.isEmpty()) ? language : "en";
    }
}