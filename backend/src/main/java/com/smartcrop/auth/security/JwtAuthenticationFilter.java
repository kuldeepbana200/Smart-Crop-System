package com.smartcrop.auth.security;

import com.smartcrop.auth.service.CustomUserDetailsService;
import com.smartcrop.auth.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.lang.NonNull;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            CustomUserDetailsService userDetailsService) {

        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        logger.debug(
                "JWT filter processing: {} {}",
                request.getMethod(),
                requestURI);

        /*
         * ==========================================
         * PUBLIC ENDPOINTS
         * ==========================================
         *
         * These endpoints do not require JWT authentication.
         */
        if (isPublicEndpoint(requestURI)) {

            logger.debug("Public endpoint - skipping JWT authentication: {}",
                    requestURI);

            filterChain.doFilter(request, response);
            return;
        }

        /*
         * ==========================================
         * GET JWT FROM REQUEST
         * ==========================================
         */

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null ||
                !authorizationHeader.startsWith("Bearer ")) {

            logger.debug("No Bearer token found");

            // Don't reject here.
            // Spring Security will decide whether the endpoint
            // requires authentication.
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7);

        try {

            /*
             * ==========================================
             * EXTRACT USERNAME
             * ==========================================
             */

            String username = jwtService.extractUsername(token);

            if (username == null) {

                logger.warn("Could not extract username from JWT");

                filterChain.doFilter(request, response);
                return;
            }

            /*
             * ==========================================
             * AUTHENTICATE USER
             * ==========================================
             */

            if (SecurityContextHolder
                    .getContext()
                    .getAuthentication() == null) {

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                /*
                 * ==========================================
                 * VALIDATE TOKEN
                 * ==========================================
                 */

                if (jwtService.isTokenValid(token, userDetails)) {

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request));

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authentication);

                    logger.debug(
                            "JWT authentication successful for user: {}",
                            username);

                } else {

                    logger.warn(
                            "Invalid JWT token for user: {}",
                            username);
                }
            }

        } catch (Exception exception) {

            /*
             * Invalid JWTs remain unauthenticated.
             * Spring Security will return 401/403 depending
             * on the endpoint/security configuration.
             */

            logger.warn(
                    "JWT authentication failed: {}",
                    exception.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Determines whether the endpoint should bypass JWT authentication.
     */
    private boolean isPublicEndpoint(String requestURI) {

        return requestURI.equals("/api/auth/register")
                || requestURI.equals("/api/auth/login")
                || requestURI.equals("/api/auth/send-otp")
                || requestURI.equals("/api/auth/verify-otp")
                || requestURI.equals("/api/auth/resend-otp")
                || requestURI.equals("/swagger-ui.html")
                || requestURI.startsWith("/swagger-ui/")
                || requestURI.equals("/v3/api-docs")
                || requestURI.startsWith("/v3/api-docs/")
                || requestURI.equals("/error");
    }
}