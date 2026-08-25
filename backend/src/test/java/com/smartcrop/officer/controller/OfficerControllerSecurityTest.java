package com.smartcrop.officer.controller;

import com.smartcrop.auth.security.JwtAuthenticationFilter;
import com.smartcrop.auth.service.CustomUserDetailsService;
import com.smartcrop.auth.service.JwtService;
import com.smartcrop.distress.dto.DistressAlertResponse;
import com.smartcrop.distress.entity.AlertStatus;
import com.smartcrop.distress.service.DistressAlertService;
import com.smartcrop.officer.dto.OfficerDashboardResponse;
import com.smartcrop.officer.service.OfficerDashboardService;
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringJUnitWebConfig(classes = OfficerControllerSecurityTest.TestConfiguration.class)
class OfficerControllerSecurityTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private OfficerDashboardService dashboardService;

    @Autowired
    private DistressAlertService alertService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        when(dashboardService.getDashboard()).thenReturn(new OfficerDashboardResponse(
                new OfficerDashboardResponse.Summary(0, 0, 0, 0, 0),
                java.util.List.of(), java.util.List.of(), java.util.List.of(), java.util.List.of()));
        when(alertService.assign(org.mockito.ArgumentMatchers.eq(30L), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new DistressAlertResponse(
                        30L, 10L, 20L, "Rice", 60, "HIGH", "HEAT", java.util.List.of(),
                        "Monitor.", AlertStatus.OPEN, 2L, null, null, null, null));
    }

    @Test
    void officerCanAccessDashboard() throws Exception {
        mockMvc.perform(get("/api/officer/dashboard")
                .with(user("officer@example.com").roles("OFFICER")))
                .andExpect(status().isOk());
    }

    @Test
    void adminCanAccessDashboard() throws Exception {
        mockMvc.perform(get("/api/officer/dashboard")
                .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void farmerCannotAccessDashboardOrAssign() throws Exception {
        mockMvc.perform(get("/api/officer/dashboard")
                .with(user("farmer@example.com").roles("FARMER")))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/officer/alerts/30/assign")
                .with(user("farmer@example.com").roles("FARMER"))
                .contentType("application/json")
                .content("{\"officerId\":2}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedUserCannotAccessOfficerEndpoints() throws Exception {
        mockMvc.perform(get("/api/officer/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void officerCanAssignAlert() throws Exception {
        mockMvc.perform(patch("/api/officer/alerts/30/assign")
                .with(user("officer@example.com").roles("OFFICER"))
                .contentType("application/json")
                .content("{\"officerId\":2}"))
                .andExpect(status().isOk());
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebMvc
    @EnableWebSecurity
    @EnableMethodSecurity
    static class TestConfiguration {

        @Bean
        OfficerDashboardController officerDashboardController(OfficerDashboardService service) {
            return new OfficerDashboardController(service);
        }

        @Bean
        OfficerAlertController officerAlertController(DistressAlertService service) {
            return new OfficerAlertController(service);
        }

        @Bean
        OfficerExceptionHandler officerExceptionHandler() {
            return new OfficerExceptionHandler();
        }

        @Bean
        OfficerDashboardService officerDashboardService() {
            return mock(OfficerDashboardService.class);
        }

        @Bean
        DistressAlertService distressAlertService() {
            return mock(DistressAlertService.class);
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
            return http.csrf(csrf -> csrf.disable())
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