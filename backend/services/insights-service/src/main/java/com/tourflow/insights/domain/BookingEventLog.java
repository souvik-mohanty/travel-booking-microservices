package com.tourflow.analytics.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

// One row per BookingConfirmed event actually processed. Append-only, same
// as audit-service's AuditLog -- this is the raw source of truth that every
// aggregate/report is computed from at read time (see AnalyticsService),
// rather than a separately maintained running-total table that could drift.
@Entity
@Table(name = "booking_event_log")
public class BookingEventLog {

    @Id
    @Column(nullable = false)
    private UUID id;

    // Unique per the DB constraint -- lets a redelivered/reprocessed
    // BookingConfirmed be detected and skipped instead of double-counted.
    // See docs/events/EVENT-SCHEMAS.md's "known gap" note: notification-service
    // doesn't do this dedup yet, this is the first consumer that does.
    @Column(name = "event_id", nullable = false, unique = true)
    private UUID eventId;

    @Column(name = "booking_id", nullable = false)
    private UUID bookingId;

    @Column(name = "tour_id", nullable = false)
    private UUID tourId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(nullable = false)
    private String currency;

    // When the underlying BookingConfirmed domain event happened (the
    // envelope's occurredAt), not when this row was written.
    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;

    @Column(name = "processed_at", nullable = false)
    private OffsetDateTime processedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (processedAt == null) {
            processedAt = OffsetDateTime.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public void setBookingId(UUID bookingId) {
        this.bookingId = bookingId;
    }

    public UUID getTourId() {
        return tourId;
    }

    public void setTourId(UUID tourId) {
        this.tourId = tourId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(OffsetDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    public OffsetDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(OffsetDateTime processedAt) {
        this.processedAt = processedAt;
    }
}
