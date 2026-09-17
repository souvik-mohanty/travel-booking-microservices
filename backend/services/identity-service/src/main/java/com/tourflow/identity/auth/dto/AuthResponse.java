package com.tourflow.identity.auth.dto;

import java.util.UUID;

// accessToken: short-lived JWT sent as a Bearer token on every request.
// refreshToken: long-lived opaque token exchanged at /api/auth/refresh for a new accessToken.
public record AuthResponse(
        String accessToken,
        String refreshToken,
        UUID userId,
        String email,
        String role
) {
}