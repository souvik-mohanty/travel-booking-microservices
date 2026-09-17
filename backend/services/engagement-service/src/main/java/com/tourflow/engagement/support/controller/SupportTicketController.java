package com.tourflow.support.controller;

import com.tourflow.support.domain.TicketStatus;
import com.tourflow.support.dto.CreateTicketRequest;
import com.tourflow.support.dto.TicketResponse;
import com.tourflow.support.service.SupportTicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
public class SupportTicketController {

    private final SupportTicketService ticketService;

    public SupportTicketController(SupportTicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(
            @Valid @RequestBody CreateTicketRequest request,
            Authentication authentication
    ) {

        UUID userId = UUID.fromString(authentication.getName());

        TicketResponse response = ticketService.createTicket(request, userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/mine")
    public ResponseEntity<List<TicketResponse>> getMyTickets(
            Authentication authentication
    ) {

        UUID userId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                ticketService.getMyTickets(userId)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicket(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                ticketService.getTicket(id)
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TicketResponse> setStatus(
            @PathVariable UUID id,
            @RequestParam TicketStatus status,
            Authentication authentication
    ) {

        UUID userId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                ticketService.setStatus(id, userId, status)
        );
    }
}
