package com.tourflow.booking.service;

import com.tourflow.booking.client.TourClient;
import com.tourflow.booking.domain.Booking;
import com.tourflow.booking.domain.BookingStatus;
import com.tourflow.booking.dto.BookingResponse;
import com.tourflow.booking.dto.CreateBookingRequest;
import com.tourflow.booking.dto.TourResponse;
import com.tourflow.booking.event.BookingEventPublisher;
import com.tourflow.booking.exception.BookingAccessDeniedException;
import com.tourflow.booking.exception.BookingNotFoundException;
import com.tourflow.booking.exception.InvalidBookingStateException;
import com.tourflow.booking.repository.BookingRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

// Business logic for creating, retrieving, and managing the lifecycle of bookings.
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TourClient tourClient;
    private final BookingEventPublisher eventPublisher;

    public BookingService(
            BookingRepository bookingRepository,
            TourClient tourClient,
            BookingEventPublisher eventPublisher
    ) {
        this.bookingRepository = bookingRepository;
        this.tourClient = tourClient;
        this.eventPublisher = eventPublisher;
    }

    // Create a new booking for the authenticated user.
    public BookingResponse createBooking(
            CreateBookingRequest request,
            UUID userId,
            String token
    ) {

        // Get the tour from tour-service, forwarding the caller's JWT since
        // tour-service's /api/tours/** requires authentication.
        TourResponse tour = tourClient.getTour(request.tourId(), token);

        // Use the actual price defined by tour-service.
        BigDecimal pricePerParticipant = tour.price();

        // Calculate the total booking price.
        BigDecimal totalPrice = pricePerParticipant.multiply(
                BigDecimal.valueOf(request.numberOfParticipants())
        );

        OffsetDateTime now = OffsetDateTime.now();

        // Create the booking entity.
        Booking booking = new Booking(
                UUID.randomUUID(),
                request.tourId(),
                userId,
                request.numberOfParticipants(),
                totalPrice,
                BookingStatus.PENDING,
                now,
                now
        );

        // Persist the booking.
        Booking savedBooking = bookingRepository.save(booking);

        return BookingResponse.fromEntity(savedBooking);
    }

    // Return a booking by its ID.
    public BookingResponse getBooking(UUID id) {

        Booking booking = findOrThrow(id);

        return BookingResponse.fromEntity(booking);
    }

    // Return all bookings belonging to a user.
    public List<BookingResponse> getUserBookings(UUID userId) {

        return bookingRepository.findByUserId(userId)
                .stream()
                .map(BookingResponse::fromEntity)
                .toList();
    }

    // Mark a booking as paid. Called by Payment Service (via a service-role
    // token, see SecurityConfig) after a payment is captured -- never by a
    // tourist directly.
    public BookingResponse markBookingPaid(UUID bookingId) {

        Booking booking = findOrThrow(bookingId);

        // Idempotent: Payment Service may retry this call (e.g. after a
        // webhook replay), so an already-PAID booking is a no-op.
        if (booking.getStatus() == BookingStatus.PAID) {
            return BookingResponse.fromEntity(booking);
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new InvalidBookingStateException(
                    "Cannot mark booking as paid from status " + booking.getStatus()
            );
        }

        booking.setStatus(BookingStatus.PAID);
        booking.setUpdatedAt(OffsetDateTime.now());

        Booking saved = bookingRepository.save(booking);

        eventPublisher.publishBookingConfirmed(saved);

        return BookingResponse.fromEntity(saved);
    }

    // Mark a booking as completed once the tour/activity has happened.
    // Only the tour's creator may do this.
    public BookingResponse completeBooking(
            UUID bookingId,
            UUID callerId,
            String token
    ) {

        Booking booking = findOrThrow(bookingId);

        TourResponse tour = tourClient.getTour(booking.getTourId(), token);

        if (!tour.createdBy().equals(callerId)) {
            throw new BookingAccessDeniedException(
                    "Only the tour's creator can mark this booking as completed"
            );
        }

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            return BookingResponse.fromEntity(booking);
        }

        if (booking.getStatus() != BookingStatus.PAID) {
            throw new InvalidBookingStateException(
                    "Only a paid booking can be marked as completed, current status is "
                            + booking.getStatus()
            );
        }

        booking.setStatus(BookingStatus.COMPLETED);
        booking.setUpdatedAt(OffsetDateTime.now());

        return BookingResponse.fromEntity(bookingRepository.save(booking));
    }

    // Cancel a booking. Only the tourist who made it may do this.
    public BookingResponse cancelBooking(UUID bookingId, UUID callerId) {

        Booking booking = findOrThrow(bookingId);

        if (!booking.getUserId().equals(callerId)) {
            throw new BookingAccessDeniedException(
                    "You cannot cancel another user's booking"
            );
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return BookingResponse.fromEntity(booking);
        }

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new InvalidBookingStateException(
                    "A completed booking cannot be cancelled"
            );
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setUpdatedAt(OffsetDateTime.now());

        return BookingResponse.fromEntity(bookingRepository.save(booking));
    }

    private Booking findOrThrow(UUID id) {

        return bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found"));
    }
}
