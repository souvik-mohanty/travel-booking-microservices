package com.tourflow.support.service;

import com.tourflow.support.domain.SupportTicket;
import com.tourflow.support.domain.TicketStatus;
import com.tourflow.support.dto.CreateTicketRequest;
import com.tourflow.support.dto.TicketResponse;
import com.tourflow.support.exception.InvalidTicketStateException;
import com.tourflow.support.exception.TicketAccessDeniedException;
import com.tourflow.support.exception.TicketNotFoundException;
import com.tourflow.support.repository.SupportTicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

// Core support-ticket logic.
//
// Status transitions are self-service (the ticket's own owner can move it)
// because there's no support-agent/ops authorization model in this system
// yet -- see the same reasoning in DriverStatus/GuideStatus.
@Service
public class SupportTicketService {

    private final SupportTicketRepository ticketRepository;

    public SupportTicketService(SupportTicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public TicketResponse createTicket(
            CreateTicketRequest request,
            UUID userId
    ) {

        OffsetDateTime now = OffsetDateTime.now();

        SupportTicket ticket = new SupportTicket(
                UUID.randomUUID(),
                userId,
                request.subject(),
                request.description(),
                TicketStatus.OPEN,
                now,
                now
        );

        SupportTicket saved = ticketRepository.save(ticket);

        return TicketResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicket(UUID id) {

        SupportTicket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found"));

        return TicketResponse.fromEntity(ticket);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getMyTickets(UUID userId) {

        return ticketRepository.findByUserId(userId)
                .stream()
                .map(TicketResponse::fromEntity)
                .toList();
    }

    @Transactional
    public TicketResponse setStatus(
            UUID ticketId,
            UUID userId,
            TicketStatus status
    ) {

        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found"));

        if (!ticket.getUserId().equals(userId)) {
            throw new TicketAccessDeniedException(
                    "You cannot update another user's ticket"
            );
        }

        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new InvalidTicketStateException(
                    "A closed ticket cannot be updated further"
            );
        }

        if (ticket.getStatus() == status) {
            return TicketResponse.fromEntity(ticket);
        }

        ticket.setStatus(status);
        ticket.setUpdatedAt(OffsetDateTime.now());

        return TicketResponse.fromEntity(ticketRepository.save(ticket));
    }
}
