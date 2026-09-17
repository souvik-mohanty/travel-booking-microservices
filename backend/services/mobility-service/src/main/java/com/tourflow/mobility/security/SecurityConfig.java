package com.tourflow.mobility.security;

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

// Security configuration for Mobility Service (driver + fleet + ride + trip
// + tracking, merged 2026-09-06).
@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) {
        this.jwtAuthenticationFilter =
                jwtAuthenticationFilter;
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

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CorsConfigurationSource corsConfigurationSource
    ) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                .csrf(csrf ->
                        csrf.disable()
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // Monitoring endpoint remains public.
                        .requestMatchers(
                                "/actuator/health/**",
                                "/actuator/prometheus",
                                "/error"
                        ).permitAll()

                        // All Ride APIs require authentication.
                        .requestMatchers(
                                "/api/rides/**",
                                "/api/ride-bookings/**"
                        ).authenticated()

                        // All Driver APIs require authentication.
                        .requestMatchers("/api/drivers/**").authenticated()

                        // All Fleet APIs require authentication.
                        .requestMatchers("/api/vehicles/**").authenticated()

                        // Only a BUSINESS-role account can write logbook entries --
                        // service-layer ownership (must be the exact tour's owner,
                        // not just any business) is the real gate, this is
                        // defense-in-depth matching every other creation endpoint.
                        .requestMatchers(HttpMethod.POST, "/api/trips/*/log")
                        .hasRole("BUSINESS")

                        // All other Trip APIs just require authentication.
                        .requestMatchers("/api/trips/**").authenticated()

                        // All Tracking APIs require authentication.
                        .requestMatchers("/api/tracking/**").authenticated()

                        .anyRequest().authenticated()
                )

                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(
                                (request, response, authException) ->
                                        response.sendError(
                                                HttpServletResponse.SC_UNAUTHORIZED
                                        )
                        )
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
