package com.tourflow.identity.admin.dto;

import com.tourflow.identity.user.User;

import java.time.OffsetDateTime;
import java.util.UUID;

// Admin-facing view of a User -- deliberately omits passwordHash/providerId,
// which no response in this service (including the tourist's own) ever
// exposes.
public record AdminUserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String role,
        boolean enabled,
        String provider,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static AdminUserResponse from(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.isEnabled(),
                user.getProvider().name(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
