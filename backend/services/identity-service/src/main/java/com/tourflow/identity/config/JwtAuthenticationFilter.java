package com.tourflow.identity.config;

import com.tourflow.identity.auth.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

// Bridges the access tokens JwtService issues (register/login/refresh, and the
// Google OAuth2 success handler) into Spring Security: reads the "Authorization:
// Bearer <token>" header on every request and, if it's a valid unexpired JWT,
// populates the SecurityContext so @PreAuthorize/hasRole/anyRequest().authenticated()
// checks downstream see an authenticated user. Runs once per request, before
// Spring Security's own username/password filter.
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Read the Authorization header.
        String authorizationHeader = request.getHeader("Authorization");

        // Continue normally when no Bearer token is present -- the endpoint's own
        // authorizeHttpRequests rule (permitAll or authenticated) decides from here.
        if (authorizationHeader == null ||
                !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        // Remove "Bearer " and keep only the JWT.
        String token = authorizationHeader.substring(7);

        try {
            // Validate the JWT and extract the user ID.
            UUID userId = jwtService.extractUserId(token);

            // Extract the role from the JWT.
            String role = jwtService.extractRole(token);

            // Create an authenticated Spring Security identity. The principal is
            // the user's UUID (not a UserDetails) since JWT auth needs no DB lookup
            // per request -- everything Security needs is already in the token.
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );

            // Attach request information to the authentication.
            authentication.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );

            // Store the authenticated user in Spring Security's context.
            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

        } catch (JwtException | IllegalArgumentException ex) {

            // Invalid or expired JWT.
            // Clear any existing authentication and allow Spring Security
            // to handle the request as unauthenticated.
            SecurityContextHolder.clearContext();
        }

        // Continue through the remaining security filters.
        filterChain.doFilter(request, response);
    }
}
