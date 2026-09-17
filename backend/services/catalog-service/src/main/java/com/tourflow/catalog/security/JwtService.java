package com.tourflow.tour.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

// Handles verification and extraction of claims from identity-service JWTs.
@Service
public class JwtService {

    // Cryptographic key used to verify the JWT signature.
    private final SecretKey secretKey;

    // Create the verification key from the configured JWT secret.
    public JwtService(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    // Parse and verify a signed JWT.
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Extract the user's UUID from the JWT subject.
    public UUID extractUserId(String token) {
        return UUID.fromString(parseToken(token).getSubject());
    }

    // Extract the user's email from the JWT.
    public String extractEmail(String token) {
        return parseToken(token).get("email", String.class);
    }

    // Extract the user's role from the JWT.
    public String extractRole(String token) {
        return parseToken(token).get("role", String.class);
    }

    // Verify that the JWT is correctly signed and not expired.
    public boolean isValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
