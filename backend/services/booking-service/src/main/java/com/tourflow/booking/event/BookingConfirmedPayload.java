package com.tourflow.booking.event;

import java.math.BigDecimal;
import java.util.UUID;

// Payload shape for the BookingConfirmed event, matching the example in
// docs/events/EVENT-SCHEMAS.md.
public record BookingConfirmedPayload(
        UUID bookingId,
        UUID tourId,
        UUID userId,
        BigDecimal totalPrice,
        String currency
) {
}
