package com.tourflow.identity.config.oauth2;

import com.tourflow.identity.user.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.List;
import java.util.Map;

// OIDC counterpart of CustomOAuth2User. Wraps the OidcUser Spring Security built
// from Google's ID token/userinfo response so downstream code
// (OAuth2AuthenticationSuccessHandler) can get straight to our own persisted User,
// the same way CustomOAuth2User does for non-OIDC providers.
public class CustomOidcUser implements OidcUser, AppOAuth2Principal {

    private final User user;
    private final OidcUser delegate;

    public CustomOidcUser(User user, OidcUser delegate) {
        this.user = user;
        this.delegate = delegate;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public Map<String, Object> getClaims() {
        return delegate.getClaims();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return delegate.getUserInfo();
    }

    @Override
    public OidcIdToken getIdToken() {
        return delegate.getIdToken();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return delegate.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
    }

    @Override
    public String getName() {
        return user.getEmail();
    }
}
