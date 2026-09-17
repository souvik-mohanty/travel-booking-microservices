package com.tourflow.mobility.tracking.service;

import com.tourflow.mobility.tracking.domain.TripLocation;
import com.tourflow.mobility.tracking.dto.RecordLocationRequest;
import com.tourflow.mobility.tracking.dto.TripLocationResponse;
import com.tourflow.mobility.tracking.exception.NoLocationDataException;
import com.tourflow.mobility.tracking.repository.TripLocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

// Records and serves trip location points. No ownership check on write --
// this doesn't yet cross-verify the caller is the trip's assigned driver
// (that needs a driver-service lookup keyed by identity-service userId,
// deferred the same way Notification's create-for-any-recipient is: getting
// the full chain right needs more design than a scoped v1 warrants).
@Service
public class TrackingService {

    private final TripLocationRepository tripLocationRepository;

    public TrackingService(TripLocationRepository tripLocationRepository) {
        this.tripLocationRepository = tripLocationRepository;
    }

    @Transactional
    public TripLocationResponse recordLocation(
            UUID tripId,
            RecordLocationRequest request
    ) {

        TripLocation location = new TripLocation(
                UUID.randomUUID(),
                tripId,
                request.latitude(),
                request.longitude(),
                OffsetDateTime.now()
        );

        TripLocation saved = tripLocationRepository.save(location);

        return TripLocationResponse.fromEntity(saved);
    }

    // What a tourist's app would poll for the current vehicle position.
    @Transactional(readOnly = true)
    public TripLocationResponse getLatestLocation(UUID tripId) {

        TripLocation location = tripLocationRepository
                .findFirstByTripIdOrderByRecordedAtDesc(tripId)
                .orElseThrow(() ->
                        new NoLocationDataException("No location has been recorded for this trip yet")
                );

        return TripLocationResponse.fromEntity(location);
    }

    // The full route so far.
    @Transactional(readOnly = true)
    public List<TripLocationResponse> getLocationHistory(UUID tripId) {

        return tripLocationRepository.findByTripIdOrderByRecordedAtAsc(tripId)
                .stream()
                .map(TripLocationResponse::fromEntity)
                .toList();
    }
}
