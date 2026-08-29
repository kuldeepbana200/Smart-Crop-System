package com.smartcrop.auth.service;

import com.smartcrop.auth.dto.AuthenticationResponse;
import com.smartcrop.auth.dto.LoginRequest;
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

    @BeforeEach
    void setUp() {
        validRequest = new RegistrationRequest(
                "John Doe",
                "john.doe@example.com",
                "password123",
                "1234567890");
    }

    @AfterEach
    @Transactional
    void tearDown() {
        farmerRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @Transactional
    void registerFarmerCreatesUserAndFarmerProfile() {
        // Act
        AuthenticationResponse response = authService.register(validRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.userId()).isPositive();
        assertThat(response.name()).isEqualTo("John Doe");
        assertThat(response.email()).isEqualTo("john.doe@example.com");
        assertThat(response.role()).isEqualTo(Role.FARMER);

        // Verify user exists
        User user = userRepository.findById(response.userId()).orElseThrow();
        assertThat(user.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(user.getRole()).isEqualTo(Role.FARMER);

        // Verify farmer profile exists
        Farmer farmer = farmerRepository.findByUserId(user.getId()).orElseThrow();
        assertThat(farmer.getId()).isPositive();
        assertThat(farmer.getUser().getId()).isEqualTo(user.getId());
        assertThat(farmer.getDistrict()).isEqualTo(""); // placeholder
        assertThat(farmer.getState()).isEqualTo(""); // placeholder
        assertThat(farmer.getLatitude()).isNull();
        assertThat(farmer.getLongitude()).isNull();
        assertThat(farmer.getLandArea()).isNull();
    }

    @Test
    @Transactional
    void registerDuplicateEmailThrowsException() {
        // Arrange
        AuthenticationResponse firstResponse = authService.register(validRequest);
        System.out.println("First registration response: " + firstResponse);
        System.out.println("User count after first registration: " + userRepository.count());
        System.out.println("Farmer count after first registration: " + farmerRepository.count());

        // Act & Assert
        assertThrows(AuthService.DuplicateEmailException.class, () -> {
            authService.register(validRequest);
        });

        // Ensure only one user exists
        System.out.println("User count after second registration attempt: " + userRepository.count());
        System.out.println("Farmer count after second registration attempt: " + farmerRepository.count());
        assertThat(userRepository.count()).isEqualTo(1);
        // Ensure farmer profile still exists (not duplicated)
        assertThat(farmerRepository.count()).isEqualTo(1);
    }

    // Note: ADMIN/OFFICER registration is not possible with current hardcoded role.
    // If role were made configurable, we would test that no farmer is created for non-FARMER roles.
}