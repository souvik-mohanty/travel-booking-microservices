package com.tourflow.mobility.trip.service;

import com.tourflow.mobility.trip.client.BookingClient;
import com.tourflow.mobility.trip.domain.Trip;
import com.tourflow.mobility.trip.domain.TripStatus;
import com.tourflow.mobility.trip.dto.AssignTripRequest;
import com.tourflow.mobility.trip.dto.BookingResponse;
import com.tourflow.mobility.trip.dto.CreateTripRequest;
import com.tourflow.mobility.trip.dto.TripResponse;
import com.tourflow.mobility.trip.exception.InvalidTripStateException;
import com.tourflow.mobility.trip.exception.TripAccessDeniedException;
import com.tourflow.mobility.trip.exception.TripAlreadyExistsException;
import com.tourflow.mobility.trip.exception.TripNotFoundException;
import com.tourflow.mobility.trip.repository.TripRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TripService {

    private final TripRepository tripRepository;
    private final BookingClient bookingClient;

    public TripService(
            TripRepository tripRepository,
            BookingClient bookingClient
    ) {
        this.tripRepository = tripRepository;
        this.bookingClient = bookingClient;
    }

    // Schedule a trip for the caller's own booking. Booking Service is the
    // authority on whether the booking exists and belongs to the caller.
    @Transactional
    public TripResponse createTrip(
            CreateTripRequest request,
            UUID callerId,
            String token
    ) {

        BookingResponse booking = bookingClient.getBooking(request.bookingId(), token);

        if (!booking.userId().equals(callerId)) {
            throw new TripAccessDeniedException(
                    "You cannot schedule a trip for another user's booking"
            );
        }

        OffsetDateTime now = OffsetDateTime.now();

        Trip trip = new Trip(
                UUID.randomUUID(),
                booking.id(),
                callerId,
                null,
                null,
                TripStatus.SCHEDULED,
                null,
                null,
                now,
                now
        );

        try {
            trip = tripRepository.saveAndFlush(trip);
        } catch (DataIntegrityViolationException ex) {
            throw new TripAlreadyExistsException("A trip already exists for this booking");
        }

        return TripResponse.fromEntity(trip);
    }

    @Transactional(readOnly = true)
    public TripResponse getTrip(UUID id) {

        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new TripNotFoundException("Trip not found"));

        return TripResponse.fromEntity(trip);
    }

    // Resolves the trip scheduled for a given booking, if one exists yet --
    // lets a business discover which trip to log against for one of their
    // tour's bookings without already knowing the trip ID. No ownership
    // check, matching every other trip-by-ID read in this service.
    @Transactional(readOnly = true)
    public TripResponse getTripForBooking(UUID bookingId) {

        Trip trip = tripRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new TripNotFoundException("No trip scheduled for this booking yet"));

        return TripResponse.fromEntity(trip);
    }

    @Transactional(readOnly = true)
    public List<TripResponse> getMyTrips(UUID createdBy) {

        return tripRepository.findByCreatedBy(createdBy)
                .stream()
                .map(TripResponse::fromEntity)
                .toList();
    }

    // Assign a driver and vehicle. Only the trip's creator may do this.
    @Transactional
    public TripResponse assignTrip(
            UUID tripId,
            UUID callerId,
            AssignTripRequest request
    ) {

        Trip trip = requireOwnedTrip(tripId, callerId);

        if (trip.getStatus() != TripStatus.SCHEDULED) {
            throw new InvalidTripStateException(
                    "Driver/vehicle can only be assigned while a trip is SCHEDULED, current status is "
                            + trip.getStatus()
            );
        }

        trip.setDriverId(request.driverId());
        trip.setVehicleId(request.vehicleId());
        trip.setUpdatedAt(OffsetDateTime.now());

        return TripResponse.fromEntity(tripRepository.save(trip));
    }

    @Transactional
    public TripResponse startTrip(UUID tripId, UUID callerId) {

        Trip trip = requireOwnedTrip(tripId, callerId);

        if (trip.getStatus() == TripStatus.IN_PROGRESS) {
            return TripResponse.fromEntity(trip);
        }

        if (trip.getStatus() != TripStatus.SCHEDULED) {
            throw new InvalidTripStateException(
                    "Only a SCHEDULED trip can be started, current status is " + trip.getStatus()
            );
        }

        if (trip.getDriverId() == null || trip.getVehicleId() == null) {
            throw new InvalidTripStateException(
                    "A driver and vehicle must be assigned before starting the trip"
            );
        }

        OffsetDateTime now = OffsetDateTime.now();
        trip.setStatus(TripStatus.IN_PROGRESS);
        trip.setStartedAt(now);
        trip.setUpdatedAt(now);

        return TripResponse.fromEntity(tripRepository.save(trip));
    }

    @Transactional
    public TripResponse completeTrip(UUID tripId, UUID callerId) {

        Trip trip = requireOwnedTrip(tripId, callerId);

        if (trip.getStatus() == TripStatus.COMPLETED) {
            return TripResponse.fromEntity(trip);
        }

        if (trip.getStatus() != TripStatus.IN_PROGRESS) {
            throw new InvalidTripStateException(
                    "Only an IN_PROGRESS trip can be completed, current status is " + trip.getStatus()
            );
        }

        OffsetDateTime now = OffsetDateTime.now();
        trip.setStatus(TripStatus.COMPLETED);
        trip.setCompletedAt(now);
        trip.setUpdatedAt(now);

        return TripResponse.fromEntity(tripRepository.save(trip));
    }

    @Transactional
    public TripResponse cancelTrip(UUID tripId, UUID callerId) {

        Trip trip = requireOwnedTrip(tripId, callerId);

        if (trip.getStatus() == TripStatus.CANCELLED) {
            return TripResponse.fromEntity(trip);
        }

        if (trip.getStatus() == TripStatus.COMPLETED) {
            throw new InvalidTripStateException("A completed trip cannot be cancelled");
        }

        trip.setStatus(TripStatus.CANCELLED);
        trip.setUpdatedAt(OffsetDateTime.now());

        return TripResponse.fromEntity(tripRepository.save(trip));
    }

    private Trip requireOwnedTrip(UUID tripId, UUID callerId) {

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripNotFoundException("Trip not found"));

        if (!trip.getCreatedBy().equals(callerId)) {
            throw new TripAccessDeniedException(
                    "You are not authorized to manage this trip"
            );
        }

        return trip;
    }
}
