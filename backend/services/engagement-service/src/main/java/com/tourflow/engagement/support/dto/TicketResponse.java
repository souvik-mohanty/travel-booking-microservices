package com.tourflow.support.dto;

import com.tourflow.support.domain.SupportTicket;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TicketResponse(
        UUID id,
        UUID userId,
        String subject,
        String description,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static TicketResponse fromEntity(SupportTicket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getUserId(),
                ticket.getSubject(),
                ticket.getDescription(),
                ticket.getStatus().name(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt()
        );
    }
}
