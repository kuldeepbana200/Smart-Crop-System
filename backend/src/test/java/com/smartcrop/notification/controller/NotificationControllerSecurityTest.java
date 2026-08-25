package com.smartcrop.notification.controller;

import com.smartcrop.auth.security.JwtAuthenticationFilter;
import com.smartcrop.auth.service.CustomUserDetailsService;
import com.smartcrop.auth.service.JwtService;
import com.smartcrop.notification.dto.NotificationResponse;
import com.smartcrop.notification.entity.NotificationStatus;
import com.smartcrop.notification.entity.NotificationType;
import com.smartcrop.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringJUnitWebConfig(classes = NotificationControllerSecurityTest.TestConfiguration.class)
class NotificationControllerSecurityTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private NotificationService notificationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        when(notificationService.getAll(org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of(response()));
    }

    @Test
    void authenticatedUserCanListNotifications() throws Exception {
        mockMvc.perform(get("/api/notifications")
                .with(user("farmer@example.com").roles("FARMER")))
                .andExpect(status().isOk());

        verify(notificationService).getAll(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void unauthenticatedUserCannotListNotifications() throws Exception {
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isUnauthorized());
    }

    private NotificationResponse response() {
        return new NotificationResponse(
                50L, 30L, null, 10L, 20L, "Rice", NotificationType.DISTRESS_ALERT_CREATED,
                "New distress alert", "An alert was created.", NotificationStatus.UNREAD,
                null, null);
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebMvc
    @EnableWebSecurity
    @EnableMethodSecurity
    static class TestConfiguration {

        @Bean
        NotificationController notificationController(NotificationService service) {
            return new NotificationController(service);
        }

        @Bean
        NotificationExceptionHandler notificationExceptionHandler() {
            return new NotificationExceptionHandler();
        }

        @Bean
        NotificationService notificationService() {
            return mock(NotificationService.class);
        }

        @Bean
        JwtAuthenticationFilter jwtAuthenticationFilter() {
            return new JwtAuthenticationFilter(
                    new JwtService("local-development-secret-that-is-at-least-32-chars", 86_400_000),
                    mock(CustomUserDetailsService.class));
        }

        @Bean
        SecurityFilterChain securityFilterChain(
                HttpSecurity http,
                JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
            return http
                    .csrf(csrf -> csrf.disable())
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .exceptionHandling(exception -> exception
                            .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                    .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                    .addFilterBefore(jwtAuthenticationFilter,
                            org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                    .build();
        }
    }
}
