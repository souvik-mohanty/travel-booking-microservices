package com.tourflow.audit.controller;

import com.tourflow.audit.dto.AuditLogResponse;
import com.tourflow.audit.dto.CreateAuditLogRequest;
import com.tourflow.audit.service.AuditLogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @PostMapping
    public ResponseEntity<AuditLogResponse> createAuditLog(
            @Valid @RequestBody CreateAuditLogRequest request,
            Authentication authentication
    ) {

        UUID actorId = UUID.fromString(authentication.getName());

        AuditLogResponse response = auditLogService.createAuditLog(request, actorId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> getAuditLogs() {

        return ResponseEntity.ok(
                auditLogService.getAuditLogs()
        );
    }

    @GetMapping("/mine")
    public ResponseEntity<List<AuditLogResponse>> getMyAuditLogs(
            Authentication authentication
    ) {

        UUID actorId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                auditLogService.getMyAuditLogs(actorId)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuditLogResponse> getAuditLog(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                auditLogService.getAuditLog(id)
        );
    }
}
