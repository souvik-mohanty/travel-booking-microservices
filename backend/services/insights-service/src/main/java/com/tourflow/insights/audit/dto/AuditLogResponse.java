package com.tourflow.insights.audit.dto;

import com.tourflow.insights.audit.domain.AuditLog;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        UUID actorId,
        String action,
        String resourceType,
        UUID resourceId,
        String details,
        OffsetDateTime occurredAt
) {

    public static AuditLogResponse fromEntity(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getActorId(),
                log.getAction(),
                log.getResourceType(),
                log.getResourceId(),
                log.getDetails(),
                log.getOccurredAt()
        );
    }
}
