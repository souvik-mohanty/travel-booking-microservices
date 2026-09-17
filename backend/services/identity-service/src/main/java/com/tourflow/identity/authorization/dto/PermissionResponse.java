package com.tourflow.authorization.dto;

import com.tourflow.authorization.domain.Permission;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PermissionResponse(
        UUID id,
        String name,
        String description,
        OffsetDateTime createdAt
) {

    public static PermissionResponse fromEntity(Permission permission) {
        return new PermissionResponse(
                permission.getId(),
                permission.getName(),
                permission.getDescription(),
                permission.getCreatedAt()
        );
    }
}
