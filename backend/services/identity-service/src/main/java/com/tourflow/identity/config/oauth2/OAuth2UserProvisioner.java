package com.tourflow.identity.config.oauth2;

import com.tourflow.identity.user.AuthProvider;
import com.tourflow.identity.user.User;
import com.tourflow.identity.user.UserRepository;
import org.springframework.stereotype.Component;

// Shared "find the matching account or provision a new one" logic used by both
// the OIDC branch (CustomOidcUserService -- Google, since it always requests the
// "openid" scope) and the plain-OAuth2 branch (CustomOAuth2UserService -- kept
// for a future non-OIDC provider). Both need the exact same account-linking rules,
// so this is the one place that logic lives.
@Component
class OAuth2UserProvisioner {

    private final UserRepository userRepository;

    OAuth2UserProvisioner(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    User provision(AuthProvider provider, String providerId, String email, String firstName, String lastName) {
        User user = userRepository.findByProviderAndProviderId(provider, providerId)
                // Fall back to matching by email in case this address was previously
                // registered with a local password -- links the accounts instead of
                // creating a duplicate.
                .or(() -> userRepository.findByEmail(email.trim().toLowerCase()))
                .orElseGet(User::new);

        boolean isNewUser = user.getId() == null;

        user.setEmail(email.trim().toLowerCase());
        user.setProvider(provider);
        user.setProviderId(providerId);

        if (isNewUser) {
            user.setFirstName(firstName != null ? firstName : email);
            user.setLastName(lastName);
            user.setRole("TOURIST");
            user.setEnabled(true);
        }

        return userRepository.save(user);
    }
}
