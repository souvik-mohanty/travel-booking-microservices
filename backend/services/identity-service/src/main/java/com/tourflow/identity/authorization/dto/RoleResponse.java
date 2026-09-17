package com.tourflow.identity.authorization.dto;

import com.tourflow.identity.authorization.domain.Role;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RoleResponse(
        UUID id,
        String name,
        String description,
        OffsetDateTime createdAt
) {

    public static RoleResponse fromEntity(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.getCreatedAt()
        );
    }
}
