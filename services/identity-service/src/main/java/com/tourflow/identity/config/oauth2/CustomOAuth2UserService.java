package com.tourflow.identity.config.oauth2;

import com.tourflow.identity.user.AuthProvider;
import com.tourflow.identity.user.User;
import com.tourflow.identity.user.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Called by Spring Security after it has already exchanged the OAuth2 authorization
// code with Google and fetched the user's profile. Our job is just to map that
// profile onto our own `users` table: find the matching account, or create one on
// first login ("just-in-time provisioning").
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // Delegates to Spring Security to actually call Google's /userinfo endpoint.
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // Google's standard OIDC claims: "sub" is the stable, unique user id.
        String providerId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        String givenName = oAuth2User.getAttribute("given_name");
        String familyName = oAuth2User.getAttribute("family_name");

        if (email == null || providerId == null) {
            throw new OAuth2AuthenticationException("Google account did not return an email/sub claim");
        }

        User user = userRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, providerId)
                // Fall back to matching by email in case this address was previously
                // registered with a local password -- links the accounts instead of
                // creating a duplicate.
                .or(() -> userRepository.findByEmail(email.trim().toLowerCase()))
                .orElseGet(User::new);

        boolean isNewUser = user.getId() == null;

        user.setEmail(email.trim().toLowerCase());
        user.setProvider(AuthProvider.GOOGLE);
        user.setProviderId(providerId);

        if (isNewUser) {
            user.setFirstName(givenName != null ? givenName : email);
            user.setLastName(familyName);
            user.setRole("TOURIST");
            user.setEnabled(true);
        }

        User savedUser = userRepository.save(user);

        return new CustomOAuth2User(savedUser, oAuth2User.getAttributes());
    }
}
