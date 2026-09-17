package com.tourflow.identity.authorization.controller;

import com.tourflow.identity.authorization.dto.AssignRoleRequest;
import com.tourflow.identity.authorization.dto.PermissionResponse;
import com.tourflow.identity.authorization.dto.RoleResponse;
import com.tourflow.identity.authorization.service.AuthorizationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users/{userId}")
public class UserRoleController {

    private final AuthorizationService authorizationService;

    public UserRoleController(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    // Grant a role to a user.
    @PostMapping("/roles")
    public ResponseEntity<Void> assignRoleToUser(
            @PathVariable UUID userId,
            @Valid @RequestBody AssignRoleRequest request
    ) {

        authorizationService.assignRoleToUser(userId, request.roleId());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/roles")
    public ResponseEntity<List<RoleResponse>> getRolesForUser(
            @PathVariable UUID userId
    ) {

        return ResponseEntity.ok(
                authorizationService.getRolesForUser(userId)
        );
    }

    // The effective, deduplicated permission set across every role this user has.
    @GetMapping("/permissions")
    public ResponseEntity<List<PermissionResponse>> getEffectivePermissions(
            @PathVariable UUID userId
    ) {

        return ResponseEntity.ok(
                authorizationService.getEffectivePermissionsForUser(userId)
        );
    }
}
