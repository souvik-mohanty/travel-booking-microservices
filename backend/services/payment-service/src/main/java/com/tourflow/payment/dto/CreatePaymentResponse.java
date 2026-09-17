package com.tourflow.payment.dto;

import java.math.BigDecimal;
import java.util.UUID;

// Returned after creating a Razorpay order. The frontend uses these fields
// to open Razorpay Checkout; internal figures like platform fee/business
// amount aren't the paying customer's business.
public record CreatePaymentResponse(
        UUID paymentId,
        String razorpayOrderId,
        String razorpayKeyId,
        BigDecimal amount,
        String currency,
        String paymentStatus
) {
}
