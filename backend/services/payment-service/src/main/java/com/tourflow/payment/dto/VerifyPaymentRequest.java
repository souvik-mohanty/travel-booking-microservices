package com.tourflow.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

// Request body sent by the frontend after Razorpay Checkout completes.
// Field names match what Razorpay Checkout's success handler returns.
public record VerifyPaymentRequest(

        @NotBlank(message = "razorpay_order_id is required")
        @JsonProperty("razorpay_order_id")
        String razorpayOrderId,

        @NotBlank(message = "razorpay_payment_id is required")
        @JsonProperty("razorpay_payment_id")
        String razorpayPaymentId,

        @NotBlank(message = "razorpay_signature is required")
        @JsonProperty("razorpay_signature")
        String razorpaySignature
) {
}
