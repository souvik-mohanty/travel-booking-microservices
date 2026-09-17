package com.tourflow.identity.config.oauth2;

import com.tourflow.identity.user.AuthProvider;
import com.tourflow.identity.user.User;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Handles plain (non-OIDC) OAuth2 login providers -- e.g. a future GitHub login.
// NOT used for Google: Google's client registration requests the "openid" scope,
// which routes it through CustomOidcUserService instead (see SecurityConfig's
// .oidcUserService(...) vs .userService(...)). Kept around so adding a non-OIDC
// provider later doesn't require rebuilding this wiring from scratch.
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final OAuth2UserProvisioner userProvisioner;

    public CustomOAuth2UserService(OAuth2UserProvisioner userProvisioner) {
        this.userProvisioner = userProvisioner;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // Delegates to Spring Security to actually call the provider's /userinfo endpoint.
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // "sub" is the standard OIDC/OAuth2 claim for the provider's stable, unique user id.
        String providerId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        String givenName = oAuth2User.getAttribute("given_name");
        String familyName = oAuth2User.getAttribute("family_name");

        if (email == null || providerId == null) {
            throw new OAuth2AuthenticationException("OAuth2 account did not return an email/sub claim");
        }

        User savedUser = userProvisioner.provision(AuthProvider.GOOGLE, providerId, email, givenName, familyName);

        return new CustomOAuth2User(savedUser, oAuth2User.getAttributes());
    }
}
