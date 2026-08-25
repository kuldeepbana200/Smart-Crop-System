package com.smartcrop.intervention.controller;

import com.smartcrop.auth.security.JwtAuthenticationFilter;
import com.smartcrop.auth.service.CustomUserDetailsService;
import com.smartcrop.auth.service.JwtService;
import com.smartcrop.intervention.dto.InterventionResponse;
import com.smartcrop.intervention.entity.InterventionStatus;
import com.smartcrop.intervention.entity.InterventionType;
import com.smartcrop.intervention.service.InterventionService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringJUnitWebConfig(classes = InterventionControllerSecurityTest.TestConfiguration.class)
class InterventionControllerSecurityTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private InterventionService interventionService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        when(interventionService.create(eq(30L), any(), any())).thenReturn(response());
    }

    @Test
    void officerCanCreateIntervention() throws Exception {
        mockMvc.perform(post("/api/officer/alerts/30/interventions")
                        .with(user("officer@example.com").roles("OFFICER"))
                        .contentType("application/json")
                        .content("{\"type\":\"FIELD_VISIT\",\"description\":\"Visit the field.\"}"))
                .andExpect(status().isCreated());

        verify(interventionService).create(eq(30L), any(), any());
    }

    @Test
    void farmerIsForbiddenFromCreatingIntervention() throws Exception {
        mockMvc.perform(post("/api/officer/alerts/30/interventions")
                        .with(user("farmer@example.com").roles("FARMER"))
                        .contentType("application/json")
                        .content("{\"type\":\"FIELD_VISIT\",\"description\":\"Visit the field.\"}"))
                .andExpect(status().isForbidden());
    }

    private InterventionResponse response() {
        return new InterventionResponse(
                40L, 30L, 10L, 20L, "Rice", 2L, InterventionType.FIELD_VISIT,
                "Visit the field.", InterventionStatus.PLANNED, null, null, null);
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebMvc
    @EnableWebSecurity
    @EnableMethodSecurity
    static class TestConfiguration {

        @Bean
        InterventionController interventionController(InterventionService service) {
            return new InterventionController(service);
        }

        @Bean
        InterventionExceptionHandler interventionExceptionHandler() {
            return new InterventionExceptionHandler();
        }

        @Bean
        InterventionService interventionService() {
            return mock(InterventionService.class);
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
