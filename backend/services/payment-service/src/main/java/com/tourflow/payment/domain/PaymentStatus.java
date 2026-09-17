package com.tourflow.payment.domain;

// Represents the state of the customer's payment to TourFlow.
public enum PaymentStatus {

    // Payment record exists but gateway order has not yet been created.
    CREATED,

    // Razorpay/order creation has started.
    PENDING,

    // Customer successfully paid TourFlow.
    PAID,

    // Payment attempt failed.
    FAILED,

    // Customer payment has been refunded.
    REFUNDED
}
