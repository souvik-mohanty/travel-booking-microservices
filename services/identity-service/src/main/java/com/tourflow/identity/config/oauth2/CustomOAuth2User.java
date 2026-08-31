package com.tourflow.identity.config.oauth2;

import com.tourflow.identity.user.User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;

// Wraps Spring Security's generic OAuth2User so downstream code (specifically
// OAuth2AuthenticationSuccessHandler) has direct access to our own User entity
// -- the one already persisted/looked-up in the database -- instead of having
// to re-parse Google's raw attribute map.
public class CustomOAuth2User implements OAuth2User {

    private final User user;
    private final Map<String, Object> attributes;

    public CustomOAuth2User(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    public User getUser() {
        return user;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
        return java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + user.getRole()));
    }

    @Override
    public String getName() {
        return user.getEmail();
    }
}
