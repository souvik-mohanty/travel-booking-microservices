package com.tourflow.payment.dto;

import com.tourflow.payment.domain.Payment;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

// Payment information returned by the REST API.
public record PaymentResponse(
        UUID id,
        UUID bookingId,
        UUID customerId,
        BigDecimal amount,
        String currency,
        String paymentStatus,
        String payoutStatus,
        BigDecimal platformFee,
        BigDecimal businessAmount,
        String gateway,
        String gatewayOrderId,
        String gatewayPaymentId,
        OffsetDateTime paidAt,
        OffsetDateTime payoutEligibleAt,
        OffsetDateTime paidOutAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static PaymentResponse fromEntity(Payment payment) {

        return new PaymentResponse(
                payment.getId(),
                payment.getBookingId(),
                payment.getCustomerId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPaymentStatus().name(),
                payment.getPayoutStatus().name(),
                payment.getPlatformFee(),
                payment.getBusinessAmount(),
                payment.getGateway(),
                payment.getGatewayOrderId(),
                payment.getGatewayPaymentId(),
                payment.getPaidAt(),
                payment.getPayoutEligibleAt(),
                payment.getPaidOutAt(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
