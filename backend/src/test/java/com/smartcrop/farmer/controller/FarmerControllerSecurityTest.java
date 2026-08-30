package com.smartcrop.farmer.controller;

import com.smartcrop.auth.security.JwtAuthenticationFilter;
import com.smartcrop.auth.service.CustomUserDetailsService;
import com.smartcrop.auth.service.JwtService;
import com.smartcrop.distress.controller.DistressAlertController;
import com.smartcrop.distress.controller.DistressAlertExceptionHandler;
import com.smartcrop.distress.dto.AcknowledgeAlertRequest;
import com.smartcrop.distress.dto.ResolveAlertRequest;
import com.smartcrop.distress.dto.DistressAlertResponse;
import com.smartcrop.distress.entity.AlertStatus;
import com.smartcrop.distress.service.DistressAlertService;
import com.smartcrop.farmer.dto.FarmerProfileResponse;
import com.smartcrop.farmer.service.FarmerService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringJUnitWebConfig(classes = FarmerControllerSecurityTest.TestConfiguration.class)
class FarmerControllerSecurityTest {

        @Autowired
        private WebApplicationContext webApplicationContext;

        @Autowired
        private FarmerService farmerService;

        @Autowired
        private DistressAlertService distressAlertService;

        private MockMvc mockMvc;

        @BeforeEach
        void setUp() {
                mockMvc = webAppContextSetup(webApplicationContext)
                                .apply(springSecurity())
                                .build();
                when(farmerService.getMyProfile(any())).thenReturn(profile());
                when(farmerService.createProfile(any(), any())).thenReturn(profile());
                when(farmerService.updateMyProfile(any(), any())).thenReturn(profile());
        }

        @Test
        void farmerCanAccessOwnProfile() throws Exception {
                mockMvc.perform(get("/api/farmers/me").with(user("farmer@example.com").roles("FARMER")))
                                .andExpect(status().isOk());

                verify(farmerService).getMyProfile(any());
        }

        @Test
        void farmerCanUpdateOwnLocationProfile() throws Exception {
                mockMvc.perform(put("/api/farmers/me")
                                .with(user("farmer@example.com").roles("FARMER"))
                                .contentType("application/json")
                                .content("{\"district\":\"Pune\",\"state\":\"Maharashtra\",\"latitude\":18.52,\"longitude\":73.85,\"landArea\":1.0}"))
                                .andExpect(status().isOk());

                verify(farmerService).updateMyProfile(any(), any());
        }

        @Test
        void officerIsForbiddenFromFarmerProfileEndpoints() throws Exception {
                mockMvc.perform(get("/api/farmers/me").with(user("officer@example.com").roles("OFFICER")))
                                .andExpect(status().isForbidden());
                mockMvc.perform(post("/api/farmers/profile")
                                .with(user("officer@example.com").roles("OFFICER"))
                                .contentType("application/json")
                                .content("{\"district\":\"Pune\",\"state\":\"Maharashtra\"}"))
                                .andExpect(status().isForbidden());
        }

        @Test
        void adminIsForbiddenFromFarmerProfileEndpoints() throws Exception {
                mockMvc.perform(get("/api/farmers/me").with(user("admin@example.com").roles("ADMIN")))
                                .andExpect(status().isForbidden());
                mockMvc.perform(post("/api/farmers/profile")
                                .with(user("admin@example.com").roles("ADMIN"))
                                .contentType("application/json")
                                .content("{\"district\":\"Pune\",\"state\":\"Maharashtra\"}"))
                                .andExpect(status().isForbidden());
        }

        @Test
        void unauthenticatedRequestIsUnauthorized() throws Exception {
                mockMvc.perform(get("/api/farmers/me"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void farmerCannotPerformOfficerAlertTransitions() throws Exception {
                mockMvc.perform(patch("/api/officer/alerts/1/acknowledge")
                                .with(user("farmer@example.com").roles("FARMER"))
                                .contentType("application/json")
                                .content("{\"note\":\"Reviewing the case\"}"))
                                .andExpect(status().isForbidden());
                mockMvc.perform(patch("/api/officer/alerts/1/resolve")
                                .with(user("farmer@example.com").roles("FARMER"))
                                .contentType("application/json")
                                .content("{\"officerNote\":\"Reviewing the case\"}"))
                                .andExpect(status().isForbidden());
        }

        @Test
        void invalidOfficerTransitionKeepsExistingBadRequestResponse() throws Exception {
                when(distressAlertService.acknowledge(anyLong(), any(AcknowledgeAlertRequest.class), any()))
                                .thenThrow(new DistressAlertService.InvalidAlertTransitionException());

                mockMvc.perform(patch("/api/officer/alerts/1/acknowledge")
                                .with(user("officer@example.com").roles("OFFICER"))
                                .contentType("application/json")
                                .content("{\"note\":\"Reviewing the case\"}"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void resolveAcceptsOfficerNoteRequestField() throws Exception {
                when(distressAlertService.resolve(anyLong(), any(ResolveAlertRequest.class), any()))
                                .thenReturn(alertResponse());

                mockMvc.perform(patch("/api/officer/alerts/1/resolve")
                                .with(user("officer@example.com").roles("OFFICER"))
                                .contentType("application/json")
                                .content("{\"officerNote\":\"Farmer contacted and corrective action completed.\"}"))
                                .andExpect(status().isOk());

                var requestCaptor = forClass(ResolveAlertRequest.class);
                verify(distressAlertService).resolve(eq(1L), requestCaptor.capture(), any());
                assertEquals("Farmer contacted and corrective action completed.", requestCaptor.getValue().note());
        }

        private FarmerProfileResponse profile() {
                return new FarmerProfileResponse(
                                1L, "Farmer", "farmer@example.com", null, null,
                                "Pune", "Maharashtra", 18.52, 73.85, 1.0);
        }

        private DistressAlertResponse alertResponse() {
                return new DistressAlertResponse(
                                1L, 1L, 1L, "Rice", 60, "HIGH", "HEAVY_RAINFALL", List.of(),
                                "Inspect drainage.", AlertStatus.RESOLVED, 2L,
                                "Farmer contacted and corrective action completed.", null, null, null);
        }

        @Configuration(proxyBeanMethods = false)
        @EnableWebMvc
        @EnableWebSecurity
        @EnableMethodSecurity
        static class TestConfiguration {

                @Bean
                FarmerController farmerController(FarmerService farmerService) {
                        return new FarmerController(farmerService);
                }

                @Bean
                DistressAlertController distressAlertController(DistressAlertService distressAlertService) {
                        return new DistressAlertController(distressAlertService);
                }

                @Bean
                DistressAlertExceptionHandler distressAlertExceptionHandler() {
                        return new DistressAlertExceptionHandler();
                }

                @Bean
                FarmerService farmerService() {
                        return mock(FarmerService.class);
                }

                @Bean
                DistressAlertService distressAlertService() {
                        return mock(DistressAlertService.class);
                }

                @Bean
                JwtAuthenticationFilter jwtAuthenticationFilter() {
                        return new JwtAuthenticationFilter(
                                        new JwtService("local-development-secret-that-is-at-least-32-chars",
                                                        86_400_000),
                                        mock(CustomUserDetailsService.class));
                }

                @Bean
                SecurityFilterChain securityFilterChain(
                                HttpSecurity http,
                                JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
                        return http
                                        .csrf(csrf -> csrf.disable())
                                        .sessionManagement(session -> session
                                                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                        .exceptionHandling(exception -> exception
                                                        .authenticationEntryPoint(new HttpStatusEntryPoint(
                                                                        HttpStatus.UNAUTHORIZED)))
                                        .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                                        .addFilterBefore(jwtAuthenticationFilter,
                                                        org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                                        .build();
                }
        }
}
