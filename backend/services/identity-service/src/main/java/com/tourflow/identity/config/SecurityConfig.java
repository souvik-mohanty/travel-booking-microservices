package com.tourflow.identity.config;

import com.tourflow.identity.config.oauth2.CustomOAuth2UserService;
import com.tourflow.identity.config.oauth2.CustomOidcUserService;
import com.tourflow.identity.config.oauth2.OAuth2AuthenticationFailureHandler;
import com.tourflow.identity.config.oauth2.OAuth2AuthenticationSuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOidcUserService customOidcUserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // Only present when the "oauth2" profile is active with real Google credentials
    // configured (see application-oauth2.yml) -- ObjectProvider lets us ask "is it
    // there?" instead of failing to start when it isn't.
    private final ObjectProvider<ClientRegistrationRepository> clientRegistrationRepository;

    public SecurityConfig(
            CustomOAuth2UserService customOAuth2UserService,
            CustomOidcUserService customOidcUserService,
            OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler,
            OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            ObjectProvider<ClientRegistrationRepository> clientRegistrationRepository
    ) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.customOidcUserService = customOidcUserService;
        this.oAuth2AuthenticationSuccessHandler = oAuth2AuthenticationSuccessHandler;
        this.oAuth2AuthenticationFailureHandler = oAuth2AuthenticationFailureHandler;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.clientRegistrationRepository = clientRegistrationRepository;
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

    // Configure HTTP security rules for the identity service.
    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CorsConfigurationSource corsConfigurationSource
    ) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // Disable CSRF because this service exposes a REST API (no browser
                // form posts/cookies to protect) that clients call with a Bearer token.
                .csrf(csrf -> csrf.disable())

                // Define which endpoints are public and protected.
                .authorizeHttpRequests(auth -> auth

                        // Registration, login, refresh, logout, health, and error responses are public.
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/refresh",
                                "/api/auth/logout",
                                "/actuator/health",
                                "/error"
                        ).permitAll()

                        // Google's OAuth2 handshake: the "start login" redirect and the
                        // callback Google sends the authorization code back to.
                        .requestMatchers(
                                "/oauth2/authorization/**",
                                "/login/oauth2/code/**"
                        ).permitAll()

                        // Every other endpoint requires authentication.
                        .anyRequest().authenticated()
                )

                // This is a REST API, not a browser app: an unauthenticated request
                // to a protected endpoint should get a plain 401, never a redirect.
                // Without this, oauth2Login() below installs its own default entry
                // point that redirects such requests to /oauth2/authorization/google
                // (i.e. treats "no credentials" as "start a browser login"), which is
                // wrong for a Bearer-token client like a mobile app or another service.
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
                        )
                );

        // Social login via Google is opt-in: only wire it up when a
        // ClientRegistrationRepository bean actually exists (i.e. the "oauth2"
        // profile is active with real credentials -- see application-oauth2.yml).
        // Calling .oauth2Login(...) unconditionally would make this service fail
        // to start for anyone running it locally without a Google app configured.
        if (clientRegistrationRepository.getIfAvailable() != null) {
            http.oauth2Login(oauth2 -> oauth2
                    .userInfoEndpoint(userInfo -> userInfo
                            // Non-OIDC providers (plain OAuth2).
                            .userService(customOAuth2UserService)
                            // OIDC providers -- Google always lands here, since it
                            // requests the "openid" scope. Without this, Google
                            // logins skip our provisioning logic entirely.
                            .oidcUserService(customOidcUserService)
                    )
                    .successHandler(oAuth2AuthenticationSuccessHandler)
                    .failureHandler(oAuth2AuthenticationFailureHandler)
            );
        }

        http
                // IF_REQUIRED (Spring's default), not STATELESS: JWT-authenticated
                // REST calls never touch the session so they behave statelessly in
                // practice, but Google's OAuth2 login flow still needs one to hold
                // the pending authorization request (state/PKCE) between the
                // redirect to Google and the callback -- STATELESS here would break
                // that handshake.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )

                // Populate the SecurityContext from a Bearer JWT before Spring
                // Security's own username/password filter runs.
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        // Build and return the security filter chain.
        return http.build();
    }
}
