package com.smartcrop.advisory.controller;

import com.smartcrop.advisory.dto.AdvisoryResponse;
import com.smartcrop.advisory.service.AdvisoryService;
import com.smartcrop.auth.security.JwtAuthenticationFilter;
import com.smartcrop.auth.service.CustomUserDetailsService;
import com.smartcrop.auth.service.JwtService;
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
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringJUnitWebConfig(classes = AdvisoryControllerSecurityTest.TestConfiguration.class)
class AdvisoryControllerSecurityTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private AdvisoryService advisoryService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        when(advisoryService.getMyAdvisories(org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of());
    }

    @Test
    void farmerCanListAdvisories() throws Exception {
        mockMvc.perform(get("/api/advisories")
                .with(user("farmer@example.com").roles("FARMER")))
                .andExpect(status().isOk());
    }

    @Test
    void officerCannotListFarmerAdvisories() throws Exception {
        mockMvc.perform(get("/api/advisories")
                .with(user("officer@example.com").roles("OFFICER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedUserCannotListAdvisories() throws Exception {
        mockMvc.perform(get("/api/advisories"))
                .andExpect(status().isUnauthorized());
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebMvc
    @EnableWebSecurity
    @EnableMethodSecurity
    static class TestConfiguration {

        @Bean
        AdvisoryController advisoryController(AdvisoryService service) {
            return new AdvisoryController(service);
        }

        @Bean
        AdvisoryExceptionHandler advisoryExceptionHandler() {
            return new AdvisoryExceptionHandler();
        }

        @Bean
        AdvisoryService advisoryService() {
            return mock(AdvisoryService.class);
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
