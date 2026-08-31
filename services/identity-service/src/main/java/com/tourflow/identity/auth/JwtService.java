package com.tourflow.identity.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

// Issues and validates short-lived JWT access tokens. This class only ever deals
// with the access token; the separate, longer-lived, revocable refresh token is
// handled by RefreshTokenService -- the two are intentionally different token
// types with different trust models (see comments in application.yml).
@Service
public class JwtService {

    // JWT secret used to sign and verify tokens.
    private final SecretKey secretKey;

    // JWT expiration time in milliseconds.
    private final long expirationMs;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs
    ) {
        // Convert the configured secret into a cryptographic signing key.
        this.secretKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );

        // Store the configured token expiration time.
        this.expirationMs = expirationMs;
    }

    // Generate a JWT for an authenticated user.
    public String generateToken(UUID userId, String email, String role) {

        // Get the current time.
        Date now = new Date();

        // Calculate when the JWT should expire.
        Date expiration = new Date(now.getTime() + expirationMs);

        // Build and sign the JWT.
        return Jwts.builder()
                // Store the user's ID as the JWT subject.
                .subject(userId.toString())

                // Store the user's email as a custom claim.
                .claim("email", email)

                // Store the user's role as a custom claim.
                .claim("role", role)

                // Set the token creation time.
                .issuedAt(now)

                // Set the token expiration time.
                .expiration(expiration)

                // Sign the token using our secret key.
                .signWith(secretKey)

                // Convert the JWT into its compact String representation.
                .compact();
    }

    // Extract the user ID from a JWT.
    public UUID extractUserId(String token) {

        // Parse and verify the signed JWT.
        String subject = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();

        // Convert the subject back into a UUID.
        return UUID.fromString(subject);
    }

    // Extract the role claim from a JWT. Used by JwtAuthenticationFilter to build
    // the ROLE_* GrantedAuthority for the request's SecurityContext.
    public String extractRole(String token) {

        // Parse and verify the signed JWT.
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }
}