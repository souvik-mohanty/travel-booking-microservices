package com.tourflow.hotel.domain;

public enum RoomStatus {
    DRAFT,
    ACTIVE,
    INACTIVE,

    // Soft-deleted. Historical records (bookings, payments, reviews) may
    // still reference this room type, so the row is never physically removed.
    DELETED
}
