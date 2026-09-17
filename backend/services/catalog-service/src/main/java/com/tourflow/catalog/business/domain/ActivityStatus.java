package com.tourflow.catalog.business.domain;

public enum ActivityStatus {
    DRAFT,
    ACTIVE,
    INACTIVE,

    // Soft-deleted. Historical records (bookings, payments, reviews) may
    // still reference this activity, so the row is never physically removed.
    DELETED
}
