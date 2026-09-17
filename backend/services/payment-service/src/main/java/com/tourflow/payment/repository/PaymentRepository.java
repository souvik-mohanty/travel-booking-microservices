package com.tourflow.payment.repository;

import com.tourflow.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Database operations for Payment entities.
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    // Return payments made by a customer.
    List<Payment> findByCustomerId(UUID customerId);

    // Find the payment associated with a booking.
    Optional<Payment> findByBookingId(UUID bookingId);

    // Find the payment associated with a Razorpay order, used to reconcile webhooks.
    Optional<Payment> findByGatewayOrderId(String gatewayOrderId);
}
