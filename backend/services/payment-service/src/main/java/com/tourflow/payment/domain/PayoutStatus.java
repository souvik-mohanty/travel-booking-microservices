package com.tourflow.payment.domain;

// Tracks the separate TourFlow -> Business settlement lifecycle.
public enum PayoutStatus {

    // Activity is not completed, so business cannot be paid.
    NOT_ELIGIBLE,

    // Activity completed and the payout can now be released.
    ELIGIBLE,

    // TourFlow has started processing the payout.
    PROCESSING,

    // Business successfully received its money.
    PAID,

    // Business payout attempt failed.
    FAILED
}
