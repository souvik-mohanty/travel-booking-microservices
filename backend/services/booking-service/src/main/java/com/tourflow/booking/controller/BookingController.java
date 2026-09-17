package com.tourflow.booking.controller;

import com.tourflow.booking.dto.BookingResponse;
import com.tourflow.booking.dto.CreateBookingRequest;
import com.tourflow.booking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// REST controller responsible for booking operations.
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    // Inject the booking service.
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // Create a new booking for the authenticated user.
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            Authentication authentication
    ) {

        // JwtAuthenticationFilter stores the user's UUID as the principal name
        // and the raw JWT as the credentials.
        UUID userId = UUID.fromString(authentication.getName());
        String token = (String) authentication.getCredentials();

        BookingResponse response =
                bookingService.createBooking(request, userId, token);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Return a single booking by ID.
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBooking(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                bookingService.getBooking(id)
        );
    }

    // Return all bookings belonging to the authenticated user.
    @GetMapping
    public ResponseEntity<List<BookingResponse>> getUserBookings(
            Authentication authentication
    ) {

        // Extract the authenticated user's UUID from the JWT.
        UUID userId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                bookingService.getUserBookings(userId)
        );
    }

    // Mark a booking as paid. Called by Payment Service with a service-role
    // token, not by tourists directly -- see SecurityConfig.
    @PatchMapping("/{id}/mark-paid")
    public ResponseEntity<BookingResponse> markBookingPaid(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                bookingService.markBookingPaid(id)
        );
    }

    // Mark a booking as completed once the tour has happened. Only the
    // tour's creator may do this.
    @PatchMapping("/{id}/complete")
    public ResponseEntity<BookingResponse> completeBooking(
            @PathVariable UUID id,
            Authentication authentication
    ) {

        UUID callerId = UUID.fromString(authentication.getName());
        String token = (String) authentication.getCredentials();

        return ResponseEntity.ok(
                bookingService.completeBooking(id, callerId, token)
        );
    }

    // Cancel a booking. Only the tourist who made it may do this.
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable UUID id,
            Authentication authentication
    ) {

        UUID callerId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                bookingService.cancelBooking(id, callerId)
        );
    }
}
