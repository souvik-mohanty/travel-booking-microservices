package com.tourflow.identity.auth;

import com.tourflow.identity.auth.exception.InvalidCredentialsException;
import com.tourflow.identity.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;

// Issues, rotates and revokes refresh tokens.
//
// Design: the raw token handed to the client is a random 256-bit value that is
// never stored anywhere; only its SHA-256 hash lives in the database. Every
// successful refresh revokes the old token and issues a brand new one ("rotation"),
// so a stolen-and-reused refresh token is detectable (its hash won't match a valid row).
@Service
public class RefreshTokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;

    private final long refreshExpirationMs;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${jwt.refresh-expiration-ms}") long refreshExpirationMs
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    // Create a brand new refresh token for a user (e.g. on login/register) and return
    // the raw (unhashed) value that must be sent to the client.
    @Transactional
    public String issue(User user) {
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        RefreshToken entity = new RefreshToken();
        entity.setUser(user);
        entity.setTokenHash(hash(rawToken));
        entity.setExpiresAt(OffsetDateTime.now().plusNanos(refreshExpirationMs * 1_000_000L));
        refreshTokenRepository.save(entity);

        return rawToken;
    }

    // Validate a raw refresh token, revoke it, and return the user it belonged to.
    // Callers are expected to immediately issue() a replacement (rotation).
    @Transactional
    public User consume(String rawToken) {
        RefreshToken entity = refreshTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new InvalidCredentialsException("Invalid or expired refresh token"));

        if (!entity.isValid()) {
            throw new InvalidCredentialsException("Invalid or expired refresh token");
        }

        entity.setRevoked(true);
        return entity.getUser();
    }

    // Revoke a refresh token outright (logout) without issuing a replacement.
    @Transactional
    public void revoke(String rawToken) {
        refreshTokenRepository.findByTokenHash(hash(rawToken))
                .ifPresent(entity -> entity.setRevoked(true));
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed to be available on every JVM; this can't actually happen.
            throw new IllegalStateException(e);
        }
    }
}
