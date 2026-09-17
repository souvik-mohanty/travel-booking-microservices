package com.tourflow.identity.authorization.service;

import com.tourflow.identity.authorization.domain.RolePermission;
import com.tourflow.identity.authorization.domain.UserRole;
import com.tourflow.identity.authorization.dto.PermissionResponse;
import com.tourflow.identity.authorization.dto.RoleResponse;
import com.tourflow.identity.authorization.exception.AlreadyExistsException;
import com.tourflow.identity.authorization.exception.PermissionNotFoundException;
import com.tourflow.identity.authorization.exception.RoleNotFoundException;
import com.tourflow.identity.authorization.repository.PermissionRepository;
import com.tourflow.identity.authorization.repository.RolePermissionRepository;
import com.tourflow.identity.authorization.repository.RoleRepository;
import com.tourflow.identity.authorization.repository.UserRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

// Role<->permission and user<->role assignments, and the effective-permission
// lookup other services would eventually call to authorize an action.
@Service
public class AuthorizationService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRoleRepository userRoleRepository;

    public AuthorizationService(
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            RolePermissionRepository rolePermissionRepository,
            UserRoleRepository userRoleRepository
    ) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Transactional
    public void assignPermissionToRole(UUID roleId, UUID permissionId) {

        if (!roleRepository.existsById(roleId)) {
            throw new RoleNotFoundException("Role not found");
        }

        if (!permissionRepository.existsById(permissionId)) {
            throw new PermissionNotFoundException("Permission not found");
        }

        if (rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId)) {
            throw new AlreadyExistsException("This role already has this permission");
        }

        rolePermissionRepository.save(new RolePermission(
                UUID.randomUUID(),
                roleId,
                permissionId,
                OffsetDateTime.now()
        ));
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> getPermissionsForRole(UUID roleId) {

        if (!roleRepository.existsById(roleId)) {
            throw new RoleNotFoundException("Role not found");
        }

        List<UUID> permissionIds = rolePermissionRepository.findByRoleId(roleId)
                .stream()
                .map(RolePermission::getPermissionId)
                .toList();

        return permissionRepository.findByIdIn(permissionIds)
                .stream()
                .map(PermissionResponse::fromEntity)
                .toList();
    }

    @Transactional
    public void assignRoleToUser(UUID userId, UUID roleId) {

        if (!roleRepository.existsById(roleId)) {
            throw new RoleNotFoundException("Role not found");
        }

        if (userRoleRepository.existsByUserIdAndRoleId(userId, roleId)) {
            throw new AlreadyExistsException("This user already has this role");
        }

        userRoleRepository.save(new UserRole(
                UUID.randomUUID(),
                userId,
                roleId,
                OffsetDateTime.now()
        ));
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> getRolesForUser(UUID userId) {

        List<UUID> roleIds = userRoleRepository.findByUserId(userId)
                .stream()
                .map(UserRole::getRoleId)
                .toList();

        return roleRepository.findByIdIn(roleIds)
                .stream()
                .map(RoleResponse::fromEntity)
                .toList();
    }

    // The union of every permission granted by every role assigned to this
    // user -- the actual "can this user do X" check other services would
    // eventually call.
    @Transactional(readOnly = true)
    public List<PermissionResponse> getEffectivePermissionsForUser(UUID userId) {

        List<UUID> roleIds = userRoleRepository.findByUserId(userId)
                .stream()
                .map(UserRole::getRoleId)
                .toList();

        List<UUID> permissionIds = rolePermissionRepository.findByRoleIdIn(roleIds)
                .stream()
                .map(RolePermission::getPermissionId)
                .distinct()
                .toList();

        return permissionRepository.findByIdIn(permissionIds)
                .stream()
                .map(PermissionResponse::fromEntity)
                .toList();
    }
}
