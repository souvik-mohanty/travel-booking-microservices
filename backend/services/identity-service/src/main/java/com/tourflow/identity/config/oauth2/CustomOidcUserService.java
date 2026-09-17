package com.tourflow.identity.config.oauth2;

import com.tourflow.identity.user.AuthProvider;
import com.tourflow.identity.user.User;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// OIDC counterpart of CustomOAuth2UserService. Google's client registration
// requests the "openid" scope (see application-oauth2.yml), which makes Spring
// Security treat it as an OIDC provider and route logins through this class (via
// .oidcUserService(...) in SecurityConfig) instead of the plain OAuth2UserService
// this app also registers for non-OIDC providers. Without a class wired up here,
// Google logins never reach our own user-provisioning logic at all -- Spring just
// hands back its own DefaultOidcUser, which is what was happening before this
// class existed (login "succeeded" but no row was ever written to `users`).
@Service
public class CustomOidcUserService extends OidcUserService {

    private final OAuth2UserProvisioner userProvisioner;

    public CustomOidcUserService(OAuth2UserProvisioner userProvisioner) {
        this.userProvisioner = userProvisioner;
    }

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        // Delegates to Spring Security to verify the ID token and call Google's
        // /userinfo endpoint.
        OidcUser oidcUser = super.loadUser(userRequest);

        // "sub" (the OIDC subject claim) is the stable, unique user id.
        String providerId = oidcUser.getSubject();
        String email = oidcUser.getEmail();

        if (email == null || providerId == null) {
            throw new OAuth2AuthenticationException("Google account did not return an email/sub claim");
        }

        User savedUser = userProvisioner.provision(
                AuthProvider.GOOGLE,
                providerId,
                email,
                oidcUser.getGivenName(),
                oidcUser.getFamilyName()
        );

        return new CustomOidcUser(savedUser, oidcUser);
    }
}
