package com.tourflow.catalog.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

// Extracts the Bearer JWT from each request and establishes the authenticated user.
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    // Inject the JWT service used to validate and read access tokens.
    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    // Process the Authorization header before the request reaches the controller.
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Read the Authorization header.
        String authorizationHeader = request.getHeader("Authorization");

        // Continue normally when no Bearer token was supplied.
        if (authorizationHeader == null ||
                !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        // Remove "Bearer " and keep only the JWT.
        String token = authorizationHeader.substring(7);

        try {
            // Verify the signature and expiration before trusting any claims.
            if (jwtService.isValid(token)) {

                // Extract the authenticated user's ID from the JWT subject.
                UUID userId = jwtService.extractUserId(token);

                // Extract the user's role from the JWT.
                String role = jwtService.extractRole(token);

                // Convert the role into Spring Security's ROLE_* authority format.
                var authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_" + role)
                );

                // Create an authenticated Spring Security principal.
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId.toString(),
                                null,
                                authorities
                        );

                // Store the authentication for the current request.
                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
            }

        } catch (Exception ex) {
            // Invalid tokens are not trusted.
            // The security configuration will ultimately return 401
            // when the endpoint requires authentication.
            SecurityContextHolder.clearContext();
        }

        // Continue to the remaining security filters and controller.
        filterChain.doFilter(request, response);
    }
}
