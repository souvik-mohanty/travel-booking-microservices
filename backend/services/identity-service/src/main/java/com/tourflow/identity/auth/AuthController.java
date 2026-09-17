package com.tourflow.identity.auth;

import com.tourflow.identity.auth.dto.AuthResponse;
import com.tourflow.identity.auth.dto.LoginRequest;
import com.tourflow.identity.auth.dto.RefreshTokenRequest;
import com.tourflow.identity.auth.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

// Public REST surface for local (email/password) authentication.
// Google/OAuth2 sign-in does not go through this controller: it flows through
// Spring Security's own /oauth2/authorization/google + /login/oauth2/code/google
// endpoints (see SecurityConfig + config.oauth2.*), which redirect back to the
// frontend with the same kind of access+refresh token pair issued here.
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // Service containing registration and authentication logic.
    private final AuthService authService;

    public AuthController(AuthService authService) {
        // Inject the authentication service.
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(
            @Valid @RequestBody RegisterRequest request
    ) {
        // Delegate registration to the service layer.
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(
            @Valid @RequestBody LoginRequest request
    ) {
        // Delegate authentication to the service layer.
        return authService.login(request);
    }

    // Exchanges a refresh token for a new access token (and a rotated refresh token).
    // Called by the client when its access token has expired, instead of forcing
    // the user to log in again.
    @PostMapping("/refresh")
    public AuthResponse refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        return authService.refresh(request.refreshToken());
    }

    // Revokes a refresh token, effectively ending that session. The client is
    // still responsible for discarding its locally stored access/refresh tokens.
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        authService.logout(request.refreshToken());
    }
}
