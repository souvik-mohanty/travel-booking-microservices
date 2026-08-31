package com.tourflow.identity.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // Used to look up (or detect a first-time) OAuth2 login, e.g. provider=GOOGLE, providerId=Google's "sub" claim.
    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);
}