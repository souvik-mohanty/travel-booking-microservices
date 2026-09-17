package com.tourflow.identity.admin;

import com.tourflow.identity.admin.dto.AdminUserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// Every endpoint here requires the ADMIN role -- see SecurityConfig's
// "/api/admin/**" matcher.
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    // Optional ?role=TOURIST|BUSINESS|ADMIN to browse one segment at a time.
    @GetMapping
    public ResponseEntity<List<AdminUserResponse>> getUsers(
            @RequestParam(required = false) String role
    ) {
        return ResponseEntity.ok(adminUserService.getUsers(role));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminUserResponse> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(adminUserService.getUser(id));
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<AdminUserResponse> disableUser(@PathVariable UUID id) {
        return ResponseEntity.ok(adminUserService.disableUser(id));
    }

    @PatchMapping("/{id}/enable")
    public ResponseEntity<AdminUserResponse> enableUser(@PathVariable UUID id) {
        return ResponseEntity.ok(adminUserService.enableUser(id));
    }
}
