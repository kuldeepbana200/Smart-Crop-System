package com.smartcrop.auth.config;

import com.smartcrop.auth.entity.Role;
import com.smartcrop.auth.entity.User;
import com.smartcrop.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DevelopmentOfficerInitializerTest {

    private static final String EMAIL = "officer@test.com";
    private static final String PASSWORD = "OfficerTest123!";
    private static final String NAME = "Test Agriculture Officer";

    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
    }

    @Test
    void createsOfficerWhenAbsent() throws Exception {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        initializer(environment()).run();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User createdUser = userCaptor.getValue();
        assertEquals(NAME, createdUser.getName());
        assertEquals(EMAIL, createdUser.getEmail());
        assertEquals(Role.OFFICER, createdUser.getRole());
        assertTrue(passwordEncoder.matches(PASSWORD, createdUser.getPassword()));
    }

    @Test
    void doesNothingWhenOfficerAlreadyExists() throws Exception {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);
        initializer(environment()).run();

        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any(User.class));
    }

    @Test
    void doesNothingWhenRequiredEnvironmentVariableIsMissing() throws Exception {
        Map<String, String> environment = new HashMap<>();
        environment.put("OFFICER_TEST_EMAIL", EMAIL);
        environment.put("OFFICER_TEST_NAME", NAME);
        initializer(environment).run();

        verify(userRepository, never()).existsByEmail(org.mockito.ArgumentMatchers.anyString());
        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any(User.class));
    }

    @Test
    void storesEncodedPasswordInsteadOfPlaintext() throws Exception {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        initializer(environment()).run();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        String storedPassword = userCaptor.getValue().getPassword();
        assertFalse(PASSWORD.equals(storedPassword));
        assertTrue(passwordEncoder.matches(PASSWORD, storedPassword));
    }

    private DevelopmentOfficerInitializer initializer(Map<String, String> environment) {
        return new DevelopmentOfficerInitializer(userRepository, passwordEncoder, () -> environment);
    }

    private Map<String, String> environment() {
        return Map.of(
                "OFFICER_TEST_EMAIL", EMAIL,
                "OFFICER_TEST_PASSWORD", PASSWORD,
                "OFFICER_TEST_NAME", NAME);
    }
}
