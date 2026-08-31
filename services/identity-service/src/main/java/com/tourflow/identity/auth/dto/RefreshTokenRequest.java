package com.tourflow.identity.auth.dto;

import jakarta.validation.constraints.NotBlank;

// Body for both /api/auth/refresh (get a new access token) and /api/auth/logout (revoke it).
public record RefreshTokenRequest(

        @NotBlank
        String refreshToken
) {
}
