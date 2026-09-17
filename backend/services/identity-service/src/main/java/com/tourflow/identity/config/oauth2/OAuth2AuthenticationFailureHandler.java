package com.tourflow.identity.config.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

// Mirror of OAuth2AuthenticationSuccessHandler for the failure path (user denied
// consent, Google returned an error, our own validation in CustomOAuth2UserService
// rejected the profile, etc). Redirects back to the same SPA page with an `error`
// query param instead of tokens, so the frontend can show a message instead of
// silently hanging.
@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final String authorizedRedirectUri;

    public OAuth2AuthenticationFailureHandler(
            @Value("${app.oauth2.authorized-redirect-uri}") String authorizedRedirectUri
    ) {
        this.authorizedRedirectUri = authorizedRedirectUri;
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {
        String targetUrl = UriComponentsBuilder.fromUriString(authorizedRedirectUri)
                .queryParam("error", java.net.URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8))
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
