package com.tourflow.insights.audit.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

// actorId is deliberately not accepted here -- it comes from the JWT.
// Callers with no user context (a background job, a webhook handler) would
// need a service-role token the same way payment-service mints one to call
// booking-service -- see BookingClient.markBookingPaid there.
public record CreateAuditLogRequest(

        @NotBlank
        String action,

        @NotBlank
        String resourceType,

        UUID resourceId,

        String details
) {
}
