package com.tourflow.catalog.event;

import java.util.UUID;

// Minimal on purpose: search-service only needs the ID to remove the
// document from its index.
public record TourCancelledPayload(
        UUID tourId
) {
}
