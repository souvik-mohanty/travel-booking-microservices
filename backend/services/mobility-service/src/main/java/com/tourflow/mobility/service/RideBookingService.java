package com.tourflow.mobility.service;

import com.tourflow.mobility.domain.Ride;
import com.tourflow.mobility.domain.RideBooking;
import com.tourflow.mobility.domain.RideBookingStatus;
import com.tourflow.mobility.domain.RideStatus;
import com.tourflow.mobility.dto.BookSeatsRequest;
import com.tourflow.mobility.dto.RideBookingResponse;
import com.tourflow.mobility.exception.InvalidRideStateException;
import com.tourflow.mobility.exception.RideAccessDeniedException;
import com.tourflow.mobility.exception.RideBookingNotFoundException;
import com.tourflow.mobility.exception.RideNotFoundException;
import com.tourflow.mobility.repository.RideBookingRepository;
import com.tourflow.mobility.repository.RideRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RideBookingService {

    private final RideBookingRepository rideBookingRepository;
    private final RideRepository rideRepository;

    public RideBookingService(
            RideBookingRepository rideBookingRepository,
            RideRepository rideRepository
    ) {
        this.rideBookingRepository = rideBookingRepository;
        this.rideRepository = rideRepository;
    }

    // Book seats on a ride. The ride row is locked for the duration of this
    // transaction (see RideRepository.findByIdForUpdate) so two concurrent
    // bookings can't both read the same availableSeats and oversell it.
    @Transactional
    public RideBookingResponse bookSeats(
            BookSeatsRequest request,
            UUID userId
    ) {

        Ride ride = rideRepository.findByIdForUpdate(request.rideId())
                .orElseThrow(() -> new RideNotFoundException("Ride not found"));

        if (ride.getStatus() != RideStatus.SCHEDULED) {
            throw new InvalidRideStateException(
                    "Seats can only be booked on a SCHEDULED ride, current status is "
                            + ride.getStatus()
            );
        }

        if (ride.getAvailableSeats() < request.seats()) {
            throw new InvalidRideStateException(
                    "Only " + ride.getAvailableSeats() + " seat(s) left on this ride"
            );
        }

        ride.setAvailableSeats(ride.getAvailableSeats() - request.seats());
        ride.setUpdatedAt(OffsetDateTime.now());
        rideRepository.save(ride);

        OffsetDateTime now = OffsetDateTime.now();

        RideBooking booking = new RideBooking(
                UUID.randomUUID(),
                ride.getId(),
                userId,
                request.seats(),
                RideBookingStatus.CONFIRMED,
                now,
                now
        );

        RideBooking saved = rideBookingRepository.save(booking);

        return RideBookingResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<RideBookingResponse> getMyBookings(UUID userId) {

        return rideBookingRepository.findByUserId(userId)
                .stream()
                .map(RideBookingResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RideBookingResponse> getBookingsForRide(UUID rideId) {

        return rideBookingRepository.findByRideId(rideId)
                .stream()
                .map(RideBookingResponse::fromEntity)
                .toList();
    }

    // Cancel a seat booking and give the seats back to the ride. The ride row
    // is locked the same way as bookSeats, since this also mutates availableSeats.
    @Transactional
    public RideBookingResponse cancelBooking(UUID bookingId, UUID userId) {

        RideBooking booking = rideBookingRepository.findById(bookingId)
                .orElseThrow(() -> new RideBookingNotFoundException("Ride booking not found"));

        if (!booking.getUserId().equals(userId)) {
            throw new RideAccessDeniedException(
                    "You cannot cancel another user's ride booking"
            );
        }

        if (booking.getStatus() == RideBookingStatus.CANCELLED) {
            return RideBookingResponse.fromEntity(booking);
        }

        Ride ride = rideRepository.findByIdForUpdate(booking.getRideId())
                .orElseThrow(() -> new RideNotFoundException("Ride not found"));

        ride.setAvailableSeats(ride.getAvailableSeats() + booking.getSeatsBooked());
        ride.setUpdatedAt(OffsetDateTime.now());
        rideRepository.save(ride);

        booking.setStatus(RideBookingStatus.CANCELLED);
        booking.setUpdatedAt(OffsetDateTime.now());

        return RideBookingResponse.fromEntity(rideBookingRepository.save(booking));
    }
}
