package com.tourflow.authorization.service;

import com.tourflow.authorization.domain.Permission;
import com.tourflow.authorization.dto.CreatePermissionRequest;
import com.tourflow.authorization.dto.PermissionResponse;
import com.tourflow.authorization.exception.AlreadyExistsException;
import com.tourflow.authorization.exception.PermissionNotFoundException;
import com.tourflow.authorization.repository.PermissionRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Transactional
    public PermissionResponse createPermission(CreatePermissionRequest request) {

        OffsetDateTime now = OffsetDateTime.now();

        Permission permission = new Permission(
                UUID.randomUUID(),
                request.name(),
                request.description(),
                now
        );

        try {
            permission = permissionRepository.saveAndFlush(permission);
        } catch (DataIntegrityViolationException ex) {
            throw new AlreadyExistsException(
                    "A permission named '" + request.name() + "' already exists"
            );
        }

        return PermissionResponse.fromEntity(permission);
    }

    @Transactional(readOnly = true)
    public PermissionResponse getPermission(UUID id) {

        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new PermissionNotFoundException("Permission not found"));

        return PermissionResponse.fromEntity(permission);
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> getPermissions() {

        return permissionRepository.findAll()
                .stream()
                .map(PermissionResponse::fromEntity)
                .toList();
    }
}
