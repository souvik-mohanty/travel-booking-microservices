package com.tourflow.mobility.service;

import com.tourflow.mobility.domain.Ride;
import com.tourflow.mobility.domain.RideStatus;
import com.tourflow.mobility.dto.CreateRideRequest;
import com.tourflow.mobility.dto.RideResponse;
import com.tourflow.mobility.exception.InvalidRideStateException;
import com.tourflow.mobility.exception.RideAccessDeniedException;
import com.tourflow.mobility.exception.RideNotFoundException;
import com.tourflow.mobility.repository.RideRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RideService {

    private final RideRepository rideRepository;

    public RideService(RideRepository rideRepository) {
        this.rideRepository = rideRepository;
    }

    @Transactional
    public RideResponse createRide(
            CreateRideRequest request,
            UUID createdBy
    ) {

        OffsetDateTime now = OffsetDateTime.now();

        Ride ride = new Ride(
                UUID.randomUUID(),
                createdBy,
                request.type(),
                request.pickupLocation(),
                request.dropLocation(),
                request.totalSeats(),

                // No seats booked yet.
                request.totalSeats(),

                request.pricePerSeat(),
                RideStatus.SCHEDULED,
                now,
                now
        );

        Ride saved = rideRepository.save(ride);

        return RideResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public RideResponse getRide(UUID id) {

        Ride ride = rideRepository.findById(id)
                .orElseThrow(() -> new RideNotFoundException("Ride not found"));

        return RideResponse.fromEntity(ride);
    }

    @Transactional(readOnly = true)
    public List<RideResponse> getRides() {

        return rideRepository.findAll()
                .stream()
                .map(RideResponse::fromEntity)
                .toList();
    }

    // Public search: only rides a tourist can still book a seat on.
    @Transactional(readOnly = true)
    public List<RideResponse> getAvailableRides() {

        return rideRepository.findAll()
                .stream()
                .filter(ride -> ride.getStatus() == RideStatus.SCHEDULED && ride.getAvailableSeats() > 0)
                .map(RideResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RideResponse> getMyRides(UUID createdBy) {

        return rideRepository.findByCreatedBy(createdBy)
                .stream()
                .map(RideResponse::fromEntity)
                .toList();
    }

    @Transactional
    public RideResponse cancelRide(UUID rideId, UUID createdBy) {

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("Ride not found"));

        if (!ride.getCreatedBy().equals(createdBy)) {
            throw new RideAccessDeniedException(
                    "You are not authorized to manage this ride"
            );
        }

        if (ride.getStatus() == RideStatus.CANCELLED) {
            return RideResponse.fromEntity(ride);
        }

        if (ride.getStatus() == RideStatus.COMPLETED) {
            throw new InvalidRideStateException("A completed ride cannot be cancelled");
        }

        ride.setStatus(RideStatus.CANCELLED);
        ride.setUpdatedAt(OffsetDateTime.now());

        return RideResponse.fromEntity(rideRepository.save(ride));
    }
}
