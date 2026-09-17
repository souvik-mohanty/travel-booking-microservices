package com.tourflow.tour.config;

import com.tourflow.tour.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

// Configures authentication and authorization for the Tour service.
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

    // Build the Spring Security filter chain.
    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CorsConfigurationSource corsConfigurationSource
    ) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // Disable CSRF because this service exposes a REST API.
                .csrf(csrf -> csrf.disable())

                // JWT authentication does not require an HTTP session.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Configure authorization rules.
                .authorizeHttpRequests(auth -> auth
                        // Health checks must remain publicly accessible.
                        .requestMatchers("/actuator/health").permitAll()

                        // Spring re-dispatches unhandled exceptions internally to
                        // /error to render the response. Without this, that internal
                        // dispatch gets challenged by security too (it carries no
                        // authentication of its own) and the client sees a 401
                        // instead of the real error status -- e.g. a 404 "Tour not
                        // found" would come back as 401 instead.
                        .requestMatchers("/error").permitAll()

                        // Every Tour API requires authentication.
                        .requestMatchers("/api/tours/**").authenticated()

                        // Everything else is also protected.
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
                        org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
