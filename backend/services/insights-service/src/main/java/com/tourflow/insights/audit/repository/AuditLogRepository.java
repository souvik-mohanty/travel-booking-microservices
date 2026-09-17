package com.tourflow.insights.audit.repository;

import com.tourflow.insights.audit.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    List<AuditLog> findByActorId(UUID actorId);
}
