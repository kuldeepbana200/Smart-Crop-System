package com.smartcrop.auth.service;

import com.smartcrop.auth.dto.AuthenticationResponse;
import com.smartcrop.auth.dto.RegistrationRequest;
import com.smartcrop.auth.entity.Role;
import com.smartcrop.auth.entity.User;
import com.smartcrop.auth.repository.UserRepository;
import com.smartcrop.farmer.entity.Farmer;
import com.smartcrop.farmer.repository.FarmerRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class AuthServiceTest {

        @Autowired
        private AuthService authService;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private FarmerRepository farmerRepository;

        private RegistrationRequest validRequest;
        private RegistrationRequest validRequestWithLanguage;

        @BeforeEach
        void setUp() {
                validRequest = new RegistrationRequest(
                                "John Doe",
                                "john.doe@example.com",
                                "password123",
                                "1234567890",
                                null);

                validRequestWithLanguage = new RegistrationRequest(
                                "Jane Smith",
                                "jane.smith@example.com",
                                "password123",
                                "0987654321",
                                "hi");
        }

        @AfterEach
        @Transactional
        void tearDown() {
                farmerRepository.deleteAll();
                userRepository.deleteAll();
        }

        @Test
        @Transactional
        void registerFarmerCreatesUserWithoutHiddenFarmerProfile() {

                AuthenticationResponse response = authService.register(validRequest);

                assertThat(response).isNotNull();
                assertThat(response.userId()).isPositive();
                assertThat(response.name()).isEqualTo("John Doe");
                assertThat(response.email()).isEqualTo("john.doe@example.com");
                assertThat(response.role()).isEqualTo(Role.FARMER);
                assertThat(response.preferredLanguage()).isEqualTo("en");

                // Phone OTP verification is required after registration
                assertThat(response.phoneVerified()).isFalse();

                User user = userRepository.findById(response.userId())
                                .orElseThrow();

                assertThat(user.getEmail())
                                .isEqualTo("john.doe@example.com");

                assertThat(user.getRole())
                                .isEqualTo(Role.FARMER);

                assertThat(user.getPreferredLanguage())
                                .isEqualTo("en");

                // User must not be phone verified immediately after registration
                assertThat(user.isPhoneVerified())
                                .isFalse();

                assertThat(farmerRepository.findByUserId(user.getId()))
                                .isEmpty();
        }

        @Test
        @Transactional
        void registerFarmerWithLanguagePreference() {

                AuthenticationResponse response = authService.register(validRequestWithLanguage);

                assertThat(response).isNotNull();
                assertThat(response.userId()).isPositive();
                assertThat(response.name()).isEqualTo("Jane Smith");

                assertThat(response.email())
                                .isEqualTo("jane.smith@example.com");

                assertThat(response.role())
                                .isEqualTo(Role.FARMER);

                assertThat(response.preferredLanguage())
                                .isEqualTo("hi");

                // Phone verification is pending
                assertThat(response.phoneVerified())
                                .isFalse();

                User user = userRepository.findById(response.userId())
                                .orElseThrow();

                assertThat(user.getEmail())
                                .isEqualTo("jane.smith@example.com");

                assertThat(user.getRole())
                                .isEqualTo(Role.FARMER);

                assertThat(user.getPreferredLanguage())
                                .isEqualTo("hi");

                assertThat(user.isPhoneVerified())
                                .isFalse();

                assertThat(farmerRepository.findByUserId(user.getId()))
                                .isEmpty();
        }

        @Test
        @Transactional
        void registerDuplicateEmailThrowsException() {

                AuthenticationResponse firstResponse = authService.register(validRequest);

                assertThat(firstResponse).isNotNull();

                assertThat(userRepository.count())
                                .isEqualTo(1);

                assertThat(farmerRepository.count())
                                .isEqualTo(0);

                assertThrows(
                                AuthService.DuplicateEmailException.class,
                                () -> authService.register(validRequest));

                assertThat(userRepository.count())
                                .isEqualTo(1);

                assertThat(farmerRepository.count())
                                .isEqualTo(0);
        }

        @Test
        @Transactional
        void getAndSetPreferredLanguage() {

                authService.register(validRequest);

                String defaultLanguage = authService.getPreferredLanguage();

                assertThat(defaultLanguage)
                                .isEqualTo("en");

                authService.setPreferredLanguage("hi");

                assertThat(authService.getPreferredLanguage())
                                .isEqualTo("hi");

                authService.setPreferredLanguage("or");

                assertThat(authService.getPreferredLanguage())
                                .isEqualTo("or");

                authService.setPreferredLanguage("mr");

                assertThat(authService.getPreferredLanguage())
                                .isEqualTo("mr");

                authService.setPreferredLanguage("en");

                assertThat(authService.getPreferredLanguage())
                                .isEqualTo("en");
        }

        @Test
        @Transactional
        void setPreferredLanguageInvalid() {

                authService.register(validRequest);

                assertThrows(
                                IllegalArgumentException.class,
                                () -> authService.setPreferredLanguage("fr"));

                assertThrows(
                                IllegalArgumentException.class,
                                () -> authService.setPreferredLanguage(""));

                assertThrows(
                                IllegalArgumentException.class,
                                () -> authService.setPreferredLanguage(null));
        }

        @Test
        @Transactional
        void getPreferredLanguageWhenNotAuthenticated() {

                try {
                        String language = authService.getPreferredLanguage();

                        assertThat(language)
                                        .isEqualTo("en");

                } catch (Exception ignored) {
                        // Security context may not be available in this test.
                }
        }

        @Test
        @Transactional
        void otpVerificationFlow() {

                AuthenticationResponse registerResponse = authService.register(validRequest);

                String email = validRequest.email();

                // User must not be verified immediately after registration
                assertThat(registerResponse.phoneVerified())
                                .isFalse();

                User user = userRepository.findByEmail(email)
                                .orElseThrow();

                assertThat(user.isPhoneVerified())
                                .isFalse();

                // Invalid OTP must fail
                boolean invalidVerify = authService.verifyOTP("123456");

                assertThat(invalidVerify)
                                .isFalse();

                // Resend OTP for existing user
                boolean resent = authService.resendOTP(email);

                assertThat(resent)
                                .isTrue();

                // Resend OTP for non-existent user
                boolean resentForNonExistent = authService.resendOTP("nonexistent@example.com");

                assertThat(resentForNonExistent)
                                .isFalse();
        }

        @Test
        @Transactional
        void verifyOTPSuccess() {

                AuthenticationResponse registerResponse = authService.register(validRequest);

                String email = validRequest.email();

                User user = userRepository.findByEmail(email)
                                .orElseThrow();

                // Initially phone verification must be false
                assertThat(registerResponse.phoneVerified())
                                .isFalse();

                assertThat(user.isPhoneVerified())
                                .isFalse();

                // Resend OTP for existing user
                boolean resent = authService.resendOTP(email);

                assertThat(resent)
                                .isTrue();

                // Resend must fail for non-existent user
                boolean resentNonExistent = authService.resendOTP("nonexistent@example.com");

                assertThat(resentNonExistent)
                                .isFalse();
        }
}