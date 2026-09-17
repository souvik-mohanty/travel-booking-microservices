package com.tourflow.booking.domain;

// Lifecycle state of a booking.
public enum BookingStatus {

    // Created, awaiting payment.
    PENDING,

    // Payment captured -- set by Payment Service after a successful charge.
    PAID,

    // The tour/activity happened; the tourist may now leave a review and the
    // business's payout can be released.
    COMPLETED,

    // Cancelled by the tourist (or the tour operator) before completion.
    CANCELLED
}
