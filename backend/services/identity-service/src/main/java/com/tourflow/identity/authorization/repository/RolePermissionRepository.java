package com.tourflow.identity.authorization.repository;

import com.tourflow.identity.authorization.domain.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RolePermissionRepository extends JpaRepository<RolePermission, UUID> {

    List<RolePermission> findByRoleId(UUID roleId);

    List<RolePermission> findByRoleIdIn(List<UUID> roleIds);

    boolean existsByRoleIdAndPermissionId(UUID roleId, UUID permissionId);
}
