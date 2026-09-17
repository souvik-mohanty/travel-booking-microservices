package com.tourflow.identity.authorization.controller;

import com.tourflow.identity.authorization.dto.AssignPermissionRequest;
import com.tourflow.identity.authorization.dto.CreatePermissionRequest;
import com.tourflow.identity.authorization.dto.PermissionResponse;
import com.tourflow.identity.authorization.service.AuthorizationService;
import com.tourflow.identity.authorization.service.PermissionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class PermissionController {

    private final PermissionService permissionService;
    private final AuthorizationService authorizationService;

    public PermissionController(
            PermissionService permissionService,
            AuthorizationService authorizationService
    ) {
        this.permissionService = permissionService;
        this.authorizationService = authorizationService;
    }

    @PostMapping("/api/permissions")
    public ResponseEntity<PermissionResponse> createPermission(
            @Valid @RequestBody CreatePermissionRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(permissionService.createPermission(request));
    }

    @GetMapping("/api/permissions")
    public ResponseEntity<List<PermissionResponse>> getPermissions() {

        return ResponseEntity.ok(
                permissionService.getPermissions()
        );
    }

    @GetMapping("/api/permissions/{id}")
    public ResponseEntity<PermissionResponse> getPermission(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                permissionService.getPermission(id)
        );
    }

    // Grant a permission to a role.
    @PostMapping("/api/roles/{roleId}/permissions")
    public ResponseEntity<Void> assignPermissionToRole(
            @PathVariable UUID roleId,
            @Valid @RequestBody AssignPermissionRequest request
    ) {

        authorizationService.assignPermissionToRole(roleId, request.permissionId());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
