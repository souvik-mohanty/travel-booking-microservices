package com.tourflow.identity.config.oauth2;

import com.tourflow.identity.user.User;

// Common accessor implemented by both CustomOAuth2User (plain OAuth2 providers)
// and CustomOidcUser (OIDC providers -- Google included, since it always requests
// the "openid" scope). OAuth2AuthenticationSuccessHandler casts to this instead
// of a concrete type so it doesn't need to know which branch Spring Security
// routed the login through.
public interface AppOAuth2Principal {

    User getUser();
}
