package com.tourflow.catalog.config;

import com.tourflow.catalog.security.JwtAuthenticationFilter;
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

// Configures authentication and authorization for Catalog Service (tour +
// business + hotel + guide, merged 2026-09-06).
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
                        .requestMatchers("/actuator/health", "/actuator/prometheus").permitAll()

                        // Spring re-dispatches unhandled exceptions internally to
                        // /error to render the response. Without this, that internal
                        // dispatch gets challenged by security too (it carries no
                        // authentication of its own) and the client sees a 401
                        // instead of the real error status -- e.g. a 404 "Tour not
                        // found" would come back as 401 instead.
                        .requestMatchers("/error").permitAll()

                        // Only a BUSINESS-role account may create tours -- tourists
                        // browse and book, businesses publish. Publish/cancel don't
                        // need their own role check: they're already gated to the
                        // tour's creator (see TourService), and a tourist can never
                        // become a creator now that this is here.
                        .requestMatchers(HttpMethod.POST, "/api/tours")
                        .hasRole("BUSINESS")

                        // Every other Tour API just requires authentication.
                        .requestMatchers("/api/tours/**").authenticated()

                        // Only a BUSINESS-role account may register a business --
                        // everything downstream of it (activities, tours under it,
                        // etc.) is reached by first owning one, so gating creation
                        // here is enough to keep tourists out of the whole
                        // business-owner surface without having to gate every
                        // individual write endpoint.
                        .requestMatchers(HttpMethod.POST, "/api/businesses")
                        .hasRole("BUSINESS")

                        // Moderation actions -- only an ADMIN account may suspend
                        // or reinstate a business, regardless of who owns it.
                        .requestMatchers(HttpMethod.PATCH, "/api/businesses/*/suspend")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/businesses/*/reinstate")
                        .hasRole("ADMIN")

                        // Every other Business/Activity API just requires authentication.
                        .requestMatchers(
                                "/api/businesses/**",
                                "/api/activities/**"
                        ).authenticated()

                        // Only a BUSINESS-role account may register a hotel or add
                        // room types under one -- everything else (rooms under a
                        // hotel, reservations against a room) is reached by first
                        // owning one, so gating creation here is enough.
                        .requestMatchers(HttpMethod.POST, "/api/hotels")
                        .hasRole("BUSINESS")
                        .requestMatchers(HttpMethod.POST, "/api/rooms")
                        .hasRole("BUSINESS")
                        // Only a BUSINESS-role account reserves rooms for a tour --
                        // tourists book tours, not room inventory directly.
                        .requestMatchers(HttpMethod.POST, "/api/rooms/*/reservations")
                        .hasRole("BUSINESS")

                        // Moderation actions -- only an ADMIN account may suspend
                        // or reinstate a hotel, regardless of who owns it.
                        .requestMatchers(HttpMethod.PATCH, "/api/hotels/*/suspend")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/hotels/*/reinstate")
                        .hasRole("ADMIN")

                        // Every other Hotel/Room API just requires authentication.
                        .requestMatchers(
                                "/api/hotels/**",
                                "/api/rooms/**"
                        ).authenticated()

                        // All Guide APIs require authentication.
                        .requestMatchers("/api/guides/**").authenticated()

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
