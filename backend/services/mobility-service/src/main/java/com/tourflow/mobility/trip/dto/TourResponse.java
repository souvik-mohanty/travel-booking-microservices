package com.tourflow.mobility.trip.dto;

import java.util.UUID;

// Minimal tour information required by Trip Service -- just enough to
// resolve "which business owns this trip's tour" for logbook access control.
public record TourResponse(
        UUID id,
        UUID createdBy
) {
}
