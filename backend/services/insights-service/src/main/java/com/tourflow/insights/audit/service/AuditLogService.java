package com.tourflow.audit.service;

import com.tourflow.audit.domain.AuditLog;
import com.tourflow.audit.dto.AuditLogResponse;
import com.tourflow.audit.dto.CreateAuditLogRequest;
import com.tourflow.audit.exception.AuditLogNotFoundException;
import com.tourflow.audit.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public AuditLogResponse createAuditLog(
            CreateAuditLogRequest request,
            UUID actorId
    ) {

        AuditLog log = new AuditLog(
                UUID.randomUUID(),
                actorId,
                request.action(),
                request.resourceType(),
                request.resourceId(),
                request.details(),
                OffsetDateTime.now()
        );

        AuditLog saved = auditLogRepository.save(log);

        return AuditLogResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public AuditLogResponse getAuditLog(UUID id) {

        AuditLog log = auditLogRepository.findById(id)
                .orElseThrow(() -> new AuditLogNotFoundException("Audit log entry not found"));

        return AuditLogResponse.fromEntity(log);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogs() {

        return auditLogRepository.findAll()
                .stream()
                .map(AuditLogResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getMyAuditLogs(UUID actorId) {

        return auditLogRepository.findByActorId(actorId)
                .stream()
                .map(AuditLogResponse::fromEntity)
                .toList();
    }
}
