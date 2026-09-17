package com.tourflow.payment.service;

import com.tourflow.payment.client.BookingClient;
import com.tourflow.payment.client.RazorpayClient;
import com.tourflow.payment.client.RazorpaySignatureVerifier;
import com.tourflow.payment.domain.Payment;
import com.tourflow.payment.domain.PaymentStatus;
import com.tourflow.payment.domain.PayoutStatus;
import com.tourflow.payment.dto.BookingResponse;
import com.tourflow.payment.dto.CreatePaymentRequest;
import com.tourflow.payment.dto.CreatePaymentResponse;
import com.tourflow.payment.dto.PaymentResponse;
import com.tourflow.payment.dto.VerifyPaymentRequest;
import com.tourflow.payment.exception.PaymentAccessDeniedException;
import com.tourflow.payment.exception.PaymentNotFoundException;
import com.tourflow.payment.exception.PaymentVerificationException;
import com.tourflow.payment.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

// Core payment business logic.
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingClient bookingClient;
    private final RazorpayClient razorpayClient;
    private final RazorpaySignatureVerifier signatureVerifier;
    private final ObjectMapper objectMapper;
    private final String razorpayKeyId;
    private final String razorpayKeySecret;
    private final String razorpayWebhookSecret;

    public PaymentService(
            PaymentRepository paymentRepository,
            BookingClient bookingClient,
            RazorpayClient razorpayClient,
            RazorpaySignatureVerifier signatureVerifier,
            ObjectMapper objectMapper,
            @Value("${razorpay.key-id}") String razorpayKeyId,
            @Value("${razorpay.key-secret}") String razorpayKeySecret,
            @Value("${razorpay.webhook-secret:}") String razorpayWebhookSecret
    ) {
        this.paymentRepository = paymentRepository;
        this.bookingClient = bookingClient;
        this.razorpayClient = razorpayClient;
        this.signatureVerifier = signatureVerifier;
        this.objectMapper = objectMapper;
        this.razorpayKeyId = razorpayKeyId;
        this.razorpayKeySecret = razorpayKeySecret;
        this.razorpayWebhookSecret = razorpayWebhookSecret;
    }

    // Create a payment record for a booking and open a matching Razorpay order.
    @Transactional
    public CreatePaymentResponse createPayment(
            CreatePaymentRequest request,
            UUID customerId,
            String token
    ) {

        // Never trust a client-supplied payment amount.
        // Retrieve the authoritative booking from Booking Service.
        BookingResponse booking =
                bookingClient.getBooking(request.bookingId(), token);

        // Prevent users from paying for somebody else's booking.
        if (!booking.userId().equals(customerId)) {
            throw new IllegalArgumentException(
                    "You cannot create payment for another user's booking"
            );
        }

        // Prevent duplicate payments for the same booking.
        paymentRepository.findByBookingId(request.bookingId())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Payment already exists for this booking"
                    );
                });

        BigDecimal amount = booking.totalPrice();

        // Initial example platform commission: 10%.
        // Later this should come from configuration/business rules.
        BigDecimal platformFee = amount
                .multiply(new BigDecimal("0.10"))
                .setScale(2, RoundingMode.HALF_UP);

        // Amount eventually payable to the tour business.
        BigDecimal businessAmount =
                amount.subtract(platformFee);

        OffsetDateTime now = OffsetDateTime.now();

        Payment payment = new Payment(
                UUID.randomUUID(),
                booking.id(),
                customerId,
                amount,
                "INR",
                PaymentStatus.CREATED,

                // Business is absolutely not eligible yet.
                PayoutStatus.NOT_ELIGIBLE,

                platformFee,
                businessAmount,
                now,
                now
        );

        // The findByBookingId check above is a fast pre-check, not the guarantee:
        // two concurrent requests for the same booking can both pass it before
        // either commits. uq_payments_booking_id is the actual guarantee: fall back
        // to the same client-facing error if the race is lost here instead.
        try {
            payment = paymentRepository.saveAndFlush(payment);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("Payment already exists for this booking");
        }

        long amountInPaise = amount
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();

        RazorpayClient.RazorpayOrder order = razorpayClient.createOrder(
                amountInPaise,
                payment.getCurrency(),
                payment.getId().toString()
        );

        payment.setGateway("razorpay");
        payment.setGatewayOrderId(order.id());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setUpdatedAt(OffsetDateTime.now());
        payment = paymentRepository.save(payment);

        return new CreatePaymentResponse(
                payment.getId(),
                order.id(),
                razorpayKeyId,
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPaymentStatus().name()
        );
    }

    // Verify the signature Razorpay Checkout returns to the frontend on success,
    // then mark the payment as paid.
    // noRollbackFor: an invalid signature must still persist the FAILED status
    // before PaymentVerificationException propagates to the 400 response --
    // otherwise Spring's default rollback-on-RuntimeException would silently
    // undo that save.
    @Transactional(noRollbackFor = PaymentVerificationException.class)
    public PaymentResponse verifyPayment(
            UUID paymentId,
            VerifyPaymentRequest request,
            UUID customerId
    ) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));

        if (!payment.getCustomerId().equals(customerId)) {
            throw new PaymentAccessDeniedException(
                    "Payment does not belong to the authenticated user"
            );
        }

        if (!request.razorpayOrderId().equals(payment.getGatewayOrderId())) {
            throw new PaymentVerificationException(
                    "Razorpay order ID does not match this payment"
            );
        }

        String payload = request.razorpayOrderId() + "|" + request.razorpayPaymentId();

        boolean valid = signatureVerifier.verify(
                payload,
                request.razorpaySignature(),
                razorpayKeySecret
        );

        if (!valid) {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setUpdatedAt(OffsetDateTime.now());
            paymentRepository.save(payment);

            throw new PaymentVerificationException("Payment signature verification failed");
        }

        OffsetDateTime now = OffsetDateTime.now();

        payment.setGatewayPaymentId(request.razorpayPaymentId());
        payment.setGatewaySignature(request.razorpaySignature());
        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setPaidAt(now);
        payment.setUpdatedAt(now);
        payment = paymentRepository.save(payment);

        notifyBookingPaid(payment.getBookingId());

        return PaymentResponse.fromEntity(payment);
    }

    // Reconcile payment status from a Razorpay webhook call. Webhooks are the
    // source of truth Razorpay recommends relying on, since a customer can close
    // the browser before the Checkout success callback ever calls verifyPayment.
    //
    // Idempotent by construction: Razorpay retries webhook deliveries at-least-once,
    // and a captured/failed event for an already-settled payment is a no-op rather
    // than an error, so replays and out-of-order delivery are both handled without
    // a separate processed-event log.
    @Transactional(noRollbackFor = PaymentVerificationException.class)
    public void handleWebhook(String rawBody, String signatureHeader) {

        if (razorpayWebhookSecret.isBlank()) {
            throw new PaymentVerificationException("Razorpay webhook secret is not configured");
        }

        boolean valid = signatureVerifier.verify(rawBody, signatureHeader, razorpayWebhookSecret);

        if (!valid) {
            throw new PaymentVerificationException("Invalid Razorpay webhook signature");
        }

        JsonNode root;
        try {
            root = objectMapper.readTree(rawBody);
        } catch (Exception ex) {
            throw new PaymentVerificationException("Malformed webhook payload");
        }

        String event = root.path("event").asString("");
        JsonNode entity = root.path("payload").path("payment").path("entity");
        String orderId = entity.path("order_id").asString(null);
        String gatewayPaymentId = entity.path("id").asString(null);

        if (orderId == null) {
            return;
        }

        paymentRepository.findByGatewayOrderId(orderId).ifPresent(payment -> {

            if ("payment.captured".equals(event)) {

                // Already settled by a prior verify/webhook call -- ignore the replay.
                if (payment.getPaymentStatus() == PaymentStatus.PAID) {
                    return;
                }

                OffsetDateTime now = OffsetDateTime.now();
                payment.setGatewayPaymentId(gatewayPaymentId);
                payment.setPaymentStatus(PaymentStatus.PAID);
                payment.setPaidAt(now);
                payment.setUpdatedAt(now);
                paymentRepository.save(payment);

                notifyBookingPaid(payment.getBookingId());

            } else if ("payment.failed".equals(event)) {

                // Never downgrade an already-captured payment; a failed event
                // arriving after capture (out-of-order delivery) must not overwrite it.
                if (payment.getPaymentStatus() == PaymentStatus.PAID
                        || payment.getPaymentStatus() == PaymentStatus.FAILED) {
                    return;
                }

                payment.setPaymentStatus(PaymentStatus.FAILED);
                payment.setUpdatedAt(OffsetDateTime.now());
                paymentRepository.save(payment);
            }
        });
    }

    // Tell Booking Service a payment was captured, so it can advance the
    // booking to PAID. Best-effort: Booking Service being unreachable must
    // not undo an already-successful payment (graceful degradation) -- the
    // booking would simply stay stuck at PENDING until this is retried or
    // reconciled some other way.
    private void notifyBookingPaid(UUID bookingId) {
        try {
            bookingClient.markBookingPaid(bookingId);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(UUID paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new PaymentNotFoundException("Payment not found")
                );

        return PaymentResponse.fromEntity(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getCustomerPayments(UUID customerId) {

        return paymentRepository.findByCustomerId(customerId)
                .stream()
                .map(PaymentResponse::fromEntity)
                .toList();
    }
}
