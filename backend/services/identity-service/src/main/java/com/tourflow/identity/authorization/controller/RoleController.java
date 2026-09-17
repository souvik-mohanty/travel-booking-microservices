package com.tourflow.authorization.controller;

import com.tourflow.authorization.dto.CreateRoleRequest;
import com.tourflow.authorization.dto.PermissionResponse;
import com.tourflow.authorization.dto.RoleResponse;
import com.tourflow.authorization.service.AuthorizationService;
import com.tourflow.authorization.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;
    private final AuthorizationService authorizationService;

    public RoleController(
            RoleService roleService,
            AuthorizationService authorizationService
    ) {
        this.roleService = roleService;
        this.authorizationService = authorizationService;
    }

    @PostMapping
    public ResponseEntity<RoleResponse> createRole(
            @Valid @RequestBody CreateRoleRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(roleService.createRole(request));
    }

    @GetMapping
    public ResponseEntity<List<RoleResponse>> getRoles() {

        return ResponseEntity.ok(
                roleService.getRoles()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleResponse> getRole(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                roleService.getRole(id)
        );
    }

    // Return the permissions granted by a role.
    @GetMapping("/{id}/permissions")
    public ResponseEntity<List<PermissionResponse>> getPermissionsForRole(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                authorizationService.getPermissionsForRole(id)
        );
    }
}
