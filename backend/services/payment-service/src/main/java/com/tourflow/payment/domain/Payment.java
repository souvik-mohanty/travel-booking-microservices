package com.tourflow.payment.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

// Represents both the customer payment and its associated future business payout.
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    private UUID id;

    // Booking for which the customer is paying.
    @Column(name = "booking_id", nullable = false)
    private UUID bookingId;

    // Authenticated TourFlow user making the payment.
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    // Amount collected from the customer.
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    // Customer payment lifecycle.
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 30)
    private PaymentStatus paymentStatus;

    // Business payout lifecycle.
    @Enumerated(EnumType.STRING)
    @Column(name = "payout_status", nullable = false, length = 30)
    private PayoutStatus payoutStatus;

    @Column(length = 30)
    private String gateway;

    @Column(name = "gateway_order_id")
    private String gatewayOrderId;

    @Column(name = "gateway_payment_id")
    private String gatewayPaymentId;

    @Column(name = "gateway_signature", length = 512)
    private String gatewaySignature;

    // TourFlow commission.
    @Column(name = "platform_fee", precision = 12, scale = 2)
    private BigDecimal platformFee;

    // Amount eventually paid to the tour business.
    @Column(name = "business_amount", precision = 12, scale = 2)
    private BigDecimal businessAmount;

    @Column(name = "paid_at")
    private OffsetDateTime paidAt;

    @Column(name = "payout_eligible_at")
    private OffsetDateTime payoutEligibleAt;

    @Column(name = "paid_out_at")
    private OffsetDateTime paidOutAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Payment() {
    }

    public Payment(
            UUID id,
            UUID bookingId,
            UUID customerId,
            BigDecimal amount,
            String currency,
            PaymentStatus paymentStatus,
            PayoutStatus payoutStatus,
            BigDecimal platformFee,
            BigDecimal businessAmount,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.amount = amount;
        this.currency = currency;
        this.paymentStatus = paymentStatus;
        this.payoutStatus = payoutStatus;
        this.platformFee = platformFee;
        this.businessAmount = businessAmount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public PayoutStatus getPayoutStatus() {
        return payoutStatus;
    }

    public String getGateway() {
        return gateway;
    }

    public String getGatewayOrderId() {
        return gatewayOrderId;
    }

    public String getGatewayPaymentId() {
        return gatewayPaymentId;
    }

    public BigDecimal getPlatformFee() {
        return platformFee;
    }

    public BigDecimal getBusinessAmount() {
        return businessAmount;
    }

    public OffsetDateTime getPaidAt() {
        return paidAt;
    }

    public OffsetDateTime getPayoutEligibleAt() {
        return payoutEligibleAt;
    }

    public OffsetDateTime getPaidOutAt() {
        return paidOutAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void setPayoutStatus(PayoutStatus payoutStatus) {
        this.payoutStatus = payoutStatus;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public void setGatewayOrderId(String gatewayOrderId) {
        this.gatewayOrderId = gatewayOrderId;
    }

    public void setGatewayPaymentId(String gatewayPaymentId) {
        this.gatewayPaymentId = gatewayPaymentId;
    }

    public void setGatewaySignature(String gatewaySignature) {
        this.gatewaySignature = gatewaySignature;
    }

    public void setPaidAt(OffsetDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public void setPayoutEligibleAt(OffsetDateTime payoutEligibleAt) {
        this.payoutEligibleAt = payoutEligibleAt;
    }

    public void setPaidOutAt(OffsetDateTime paidOutAt) {
        this.paidOutAt = paidOutAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
