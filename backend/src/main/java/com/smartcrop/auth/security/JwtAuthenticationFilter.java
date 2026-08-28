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

    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        logger.info("JwtAuthenticationFilter processing request: {}", requestURI);
        // Skip JWT authentication for Swagger/OpenAPI endpoints
        if (requestURI.startsWith("/swagger-ui/") || requestURI.equals("/swagger-ui.html")
                || requestURI.startsWith("/v3/api-docs/") || requestURI.equals("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        logger.info("Processing request: {} {}", request.getMethod(), requestURI);
        if (requestURI.contains("/history")) {
            logger.info("History endpoint detected");
        }

        String authorizationHeader = request.getHeader("Authorization");
        logger.info("Authorization header: {}", authorizationHeader);

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            logger.info("No Bearer token found, continuing filter chain");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7);
        logger.info("Extracted token: {}", token);

        try {
            String username = jwtService.extractUsername(token);
            logger.info("Extracted username: {}", username);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetails = userDetailsService.loadUserByUsername(username);
                logger.info("Loaded user details: {} with authorities: {}", username, userDetails.getAuthorities());

                if (jwtService.isTokenValid(token, userDetails)) {
                    logger.info("Token is valid for user: {}", username);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.info("Authentication set in SecurityContext: {}",
                            SecurityContextHolder.getContext().getAuthentication());
                } else {
                    logger.warn("Token is invalid for user: {}", username);
                }
            } else if (username == null) {
                logger.warn("Could not extract username from token");
            } else {
                logger.info("Authentication already present in SecurityContext: {}",
                        SecurityContextHolder.getContext().getAuthentication());
            }
        } catch (RuntimeException exception) {
            logger.error("Error processing JWT token", exception);
            // Invalid tokens remain unauthenticated and are handled by Spring Security.
        }

        logger.info("Final authentication in SecurityContext: {}",
                SecurityContextHolder.getContext().getAuthentication());
        filterChain.doFilter(request, response);
    }
}
