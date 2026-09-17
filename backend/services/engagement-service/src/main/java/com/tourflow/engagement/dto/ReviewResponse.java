package com.tourflow.review.dto;

import com.tourflow.review.domain.Review;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        UUID bookingId,
        UUID businessId,
        UUID activityId,
        UUID userId,
        Integer rating,
        String title,
        String comment,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static ReviewResponse fromEntity(Review review) {

        return new ReviewResponse(
                review.getId(),
                review.getBookingId(),
                review.getBusinessId(),
                review.getActivityId(),
                review.getUserId(),
                review.getRating(),
                review.getTitle(),
                review.getComment(),
                review.getStatus().name(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
