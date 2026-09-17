package com.tourflow.identity.auth;

import com.tourflow.identity.auth.dto.AuthResponse;
import com.tourflow.identity.auth.dto.LoginRequest;
import com.tourflow.identity.auth.dto.RegisterRequest;
import com.tourflow.identity.auth.exception.AccountDisabledException;
import com.tourflow.identity.auth.exception.EmailAlreadyRegisteredException;
import com.tourflow.identity.auth.exception.InvalidCredentialsException;
import com.tourflow.identity.user.AuthProvider;
import com.tourflow.identity.user.User;
import com.tourflow.identity.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Core business logic for email/password authentication: registration, login,
// and refresh-token exchange. OAuth2 (Google) sign-in is handled by a separate
// path (config.oauth2.CustomOAuth2UserService + OAuth2AuthenticationSuccessHandler)
// but both paths converge on the same JwtService/RefreshTokenService to issue tokens,
// so a client can't tell from the tokens alone which way the user signed in.
@Service
public class AuthService {

    // Repository is responsible for communicating with the users table.
    private final UserRepository userRepository;

    // PasswordEncoder is responsible for hashing and verifying passwords.
    private final PasswordEncoder passwordEncoder;

    // JwtService is responsible for generating authentication tokens.
    private final JwtService jwtService;

    // RefreshTokenService issues/validates/revokes the long-lived refresh tokens.
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService
    ) {
        // Store the user repository dependency.
        this.userRepository = userRepository;

        // Store the password encoder dependency.
        this.passwordEncoder = passwordEncoder;

        // Store the JWT service dependency.
        this.jwtService = jwtService;

        // Store the refresh token service dependency.
        this.refreshTokenService = refreshTokenService;
    }

    // Register a new user.
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        // Normalize the email before storing it.
        String email = request.email().trim().toLowerCase();

        // Prevent duplicate email registration. Mapped to HTTP 409 by GlobalExceptionHandler
        // instead of bubbling up as an unhandled IllegalArgumentException (HTTP 500).
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException("Email is already registered");
        }

        // Create a new User entity.
        User user = new User();

        // Store the normalized email.
        user.setEmail(email);

        // Hash the password before storing it.
        user.setPasswordHash(
                passwordEncoder.encode(request.password())
        );

        // Store the user's profile information.
        user.setFirstName(request.firstName().trim());

        user.setLastName(
                request.lastName() == null
                        ? null
                        : request.lastName().trim()
        );

        // New users are tourists by default.
        user.setRole("TOURIST");

        // Enable the account by default.
        user.setEnabled(true);

        // This account authenticates with a local password, not an OAuth2 provider.
        user.setProvider(AuthProvider.LOCAL);

        // Save the user to PostgreSQL.
        User savedUser = userRepository.save(user);

        // Log the user straight in after registration so the client doesn't need a
        // second round trip: issue the same access + refresh token pair login() would.
        return issueTokens(savedUser);
    }

    // Authenticate an existing user and generate a JWT + refresh token.
    @Transactional
    public AuthResponse login(LoginRequest request) {

        // Normalize the email before searching the database.
        String email = request.email().trim().toLowerCase();

        // Find the user by email. Mapped to HTTP 401 by GlobalExceptionHandler.
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new InvalidCredentialsException("Invalid email or password")
                );

        // Reject disabled accounts. Mapped to HTTP 403 by GlobalExceptionHandler.
        if (!user.isEnabled()) {
            throw new AccountDisabledException("User account is disabled");
        }

        // An OAuth2-only account (no local password set) can't be used with this endpoint.
        if (user.getPasswordHash() == null) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Compare the submitted password with the stored BCrypt hash.
        boolean passwordMatches = passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()
        );

        // Never reveal whether the email or password was incorrect.
        if (!passwordMatches) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return issueTokens(user);
    }

    // Exchange a valid, unexpired refresh token for a new access token. The refresh
    // token itself is rotated (old one revoked, new one issued) on every use.
    @Transactional
    public AuthResponse refresh(String refreshToken) {
        User user = refreshTokenService.consume(refreshToken);

        if (!user.isEnabled()) {
            throw new AccountDisabledException("User account is disabled");
        }

        return issueTokens(user);
    }

    // Revoke a refresh token (logout). The still-live access token remains valid
    // until it naturally expires, since JWTs are stateless and not tracked server-side.
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }

    // Shared by register/login/refresh: mint a fresh access token + refresh token pair.
    private AuthResponse issueTokens(User user) {
        String accessToken = jwtService.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );

        String refreshToken = refreshTokenService.issue(user);

        return new AuthResponse(
                accessToken,
                refreshToken,
                user.getId(),
                user.getEmail(),
                user.getRole()
        );
    }
}
