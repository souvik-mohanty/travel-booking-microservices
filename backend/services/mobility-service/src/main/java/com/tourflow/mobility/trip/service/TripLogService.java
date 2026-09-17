package com.tourflow.mobility.trip.service;

import com.tourflow.mobility.trip.client.BookingClient;
import com.tourflow.mobility.trip.client.TourClient;
import com.tourflow.mobility.trip.domain.Trip;
import com.tourflow.mobility.trip.domain.TripLogEntry;
import com.tourflow.mobility.trip.dto.BookingResponse;
import com.tourflow.mobility.trip.dto.CreateTripLogEntryRequest;
import com.tourflow.mobility.trip.dto.TourResponse;
import com.tourflow.mobility.trip.dto.TripLogEntryResponse;
import com.tourflow.mobility.trip.exception.ExternalServiceException;
import com.tourflow.mobility.trip.exception.TripAccessDeniedException;
import com.tourflow.mobility.trip.exception.TripNotFoundException;
import com.tourflow.mobility.trip.repository.TripLogEntryRepository;
import com.tourflow.mobility.trip.repository.TripRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

// A trip's logbook: check-in/check-out, activities completed, destination
// reached -- writable only by the business that owns the trip's tour,
// readable by that same business, the tourist who booked it, and admin
// (admin's bypass is checked by the caller, TripLogController, since role
// isn't otherwise visible down here).
@Service
public class TripLogService {

    private final TripLogEntryRepository tripLogEntryRepository;
    private final TripRepository tripRepository;
    private final BookingClient bookingClient;
    private final TourClient tourClient;

    public TripLogService(
            TripLogEntryRepository tripLogEntryRepository,
            TripRepository tripRepository,
            BookingClient bookingClient,
            TourClient tourClient
    ) {
        this.tripLogEntryRepository = tripLogEntryRepository;
        this.tripRepository = tripRepository;
        this.bookingClient = bookingClient;
        this.tourClient = tourClient;
    }

    @Transactional
    public TripLogEntryResponse addEntry(
            UUID tripId,
            CreateTripLogEntryRequest request,
            UUID callerId,
            String token
    ) {
        Trip trip = findTripOrThrow(tripId);
        TourResponse tour = resolveTour(trip, token);

        if (!tour.createdBy().equals(callerId)) {
            throw new TripAccessDeniedException(
                    "Only the business that owns this tour may add logbook entries"
            );
        }

        OffsetDateTime now = OffsetDateTime.now();

        TripLogEntry entry = new TripLogEntry(
                UUID.randomUUID(),
                tripId,
                request.type(),
                request.description(),
                request.location(),
                request.occurredAt() != null ? request.occurredAt() : now,
                callerId,
                now
        );

        return TripLogEntryResponse.fromEntity(tripLogEntryRepository.save(entry));
    }

    @Transactional(readOnly = true)
    public List<TripLogEntryResponse> getLog(UUID tripId, UUID callerId, boolean isAdmin, String token) {

        Trip trip = findTripOrThrow(tripId);

        if (!isAdmin) {
            BookingResponse booking = resolveBooking(trip, token);
            TourResponse tour = resolveTour(booking, token);

            boolean isTourist = booking.userId().equals(callerId);
            boolean isBusinessOwner = tour.createdBy().equals(callerId);

            if (!isTourist && !isBusinessOwner) {
                throw new TripAccessDeniedException(
                        "You are not authorized to view this trip's logbook"
                );
            }
        }

        return tripLogEntryRepository.findByTripIdOrderByOccurredAtAsc(tripId)
                .stream()
                .map(TripLogEntryResponse::fromEntity)
                .toList();
    }

    private Trip findTripOrThrow(UUID tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new TripNotFoundException("Trip not found"));
    }

    private TourResponse resolveTour(Trip trip, String token) {
        return resolveTour(resolveBooking(trip, token), token);
    }

    private TourResponse resolveTour(BookingResponse booking, String token) {
        try {
            return tourClient.getTour(booking.tourId(), token);
        } catch (Exception ex) {
            throw new ExternalServiceException("Unable to communicate with Tour Service", ex);
        }
    }

    private BookingResponse resolveBooking(Trip trip, String token) {
        try {
            return bookingClient.getBooking(trip.getBookingId(), token);
        } catch (Exception ex) {
            throw new ExternalServiceException("Unable to communicate with Booking Service", ex);
        }
    }
}
