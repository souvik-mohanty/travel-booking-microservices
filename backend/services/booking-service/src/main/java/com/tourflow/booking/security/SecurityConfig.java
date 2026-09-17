package com.tourflow.booking.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

// Security configuration for booking-service.
@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // Inject the JWT filter responsible for authenticating Bearer tokens.
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    // The frontend SPA runs on a different origin (port) than every backend
    // service, since api-gateway has no routes configured yet -- without this,
    // a browser blocks every request before it even reaches JwtAuthenticationFilter,
    // even though curl (which doesn't enforce CORS) sees no problem at all.
    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins}") String[] allowedOrigins
    ) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // Configure HTTP security rules.
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CorsConfigurationSource corsConfigurationSource
    ) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // REST APIs do not use browser sessions or CSRF tokens.
                .csrf(csrf -> csrf.disable())

                // JWT authentication does not require an HTTP session.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Configure endpoint authorization.
                .authorizeHttpRequests(auth -> auth
                        // Allow health checks without authentication.
                        .requestMatchers("/actuator/health/**").permitAll()

                        // Spring re-dispatches unhandled exceptions internally to
                        // /error to render the response. Without this, that internal
                        // dispatch gets challenged by security too and the client
                        // sees a 401 instead of the real error status.
                        .requestMatchers("/error").permitAll()

                        // Only Payment Service (via a minted role=SERVICE token,
                        // never a real user's) may mark a booking as paid. This
                        // must be declared before the general authenticated rule
                        // below, since Spring Security uses the first match.
                        .requestMatchers(HttpMethod.PATCH, "/api/bookings/*/mark-paid")
                        .hasRole("SERVICE")

                        // All other booking APIs require authentication.
                        .requestMatchers("/api/bookings/**").authenticated()

                        // Reject everything else by default.
                        .anyRequest().authenticated()
                )

                // Return HTTP 401 instead of redirecting or returning 403
                // when a request has no valid authentication.
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(
                                        HttpServletResponse.SC_UNAUTHORIZED,
                                        "Unauthorized"
                                )
                        )
                )

                // Authenticate Bearer JWTs before authorization takes place.
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
