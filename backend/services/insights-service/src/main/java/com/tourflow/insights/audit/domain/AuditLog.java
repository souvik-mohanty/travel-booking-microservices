package com.tourflow.insights.audit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

// Append-only: no updatedAt, no update/delete operations. An audit trail
// that could be edited after the fact isn't an audit trail.
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    private UUID id;

    // identity-service user who performed the action. Nullable for
    // system-initiated actions with no human actor.
    @Column(name = "actor_id")
    private UUID actorId;

    // e.g. "BookingCancelled", "PaymentRefunded".
    @Column(nullable = false, length = 100)
    private String action;

    // e.g. "Booking", "Payment".
    @Column(name = "resource_type", nullable = false, length = 100)
    private String resourceType;

    @Column(name = "resource_id")
    private UUID resourceId;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;

    protected AuditLog() {
    }

    public AuditLog(
            UUID id,
            UUID actorId,
            String action,
            String resourceType,
            UUID resourceId,
            String details,
            OffsetDateTime occurredAt
    ) {
        this.id = id;
        this.actorId = actorId;
        this.action = action;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.details = details;
        this.occurredAt = occurredAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getActorId() {
        return actorId;
    }

    public String getAction() {
        return action;
    }

    public String getResourceType() {
        return resourceType;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public String getDetails() {
        return details;
    }

    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }
}
