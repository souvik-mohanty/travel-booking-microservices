package com.tourflow.identity.authorization.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.OffsetDateTime;
import java.util.UUID;

// Assigns a Permission to a Role.
@Entity
@Table(
        name = "role_permissions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_role_permissions_role_permission",
                        columnNames = {"role_id", "permission_id"}
                )
        }
)
public class RolePermission {

    @Id
    private UUID id;

    @Column(name = "role_id", nullable = false)
    private UUID roleId;

    @Column(name = "permission_id", nullable = false)
    private UUID permissionId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected RolePermission() {
    }

    public RolePermission(
            UUID id,
            UUID roleId,
            UUID permissionId,
            OffsetDateTime createdAt
    ) {
        this.id = id;
        this.roleId = roleId;
        this.permissionId = permissionId;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getRoleId() {
        return roleId;
    }

    public UUID getPermissionId() {
        return permissionId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
