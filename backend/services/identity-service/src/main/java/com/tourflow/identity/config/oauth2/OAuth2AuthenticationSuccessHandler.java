package com.tourflow.identity.config.oauth2;

import com.tourflow.identity.auth.JwtService;
import com.tourflow.identity.auth.RefreshTokenService;
import com.tourflow.identity.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

// Runs once Spring Security has finished the Google OAuth2 handshake and
// CustomOAuth2UserService has resolved/created our own User row. From here on the
// flow re-joins the same token issuance used by normal login: mint a JWT access
// token + opaque refresh token, then hand them to the frontend.
//
// Because this is a browser redirect flow (not a JSON API call), the tokens can't
// be returned in a response body -- they're appended as query params on the
// redirect back to the SPA, which is the standard pattern for OAuth2 login pages.
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final String authorizedRedirectUri;

    public OAuth2AuthenticationSuccessHandler(
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            @Value("${app.oauth2.authorized-redirect-uri}") String authorizedRedirectUri
    ) {
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.authorizedRedirectUri = authorizedRedirectUri;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        // Works for both branches: CustomOAuth2User (plain OAuth2 providers) and
        // CustomOidcUser (OIDC providers -- Google, since it requests "openid").
        AppOAuth2Principal principal = (AppOAuth2Principal) authentication.getPrincipal();
        User user = principal.getUser();

        String accessToken = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = refreshTokenService.issue(user);

        String targetUrl = UriComponentsBuilder.fromUriString(authorizedRedirectUri)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build()
                .toUriString();

        // Clear anything Spring Security stashed about the pre-login request so a
        // stale saved request can't hijack the next navigation.
        clearAuthenticationAttributes(request);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
