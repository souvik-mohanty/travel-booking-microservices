package com.tourflow.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

// userId is deliberately not accepted here -- it comes from the JWT.
// businessId IS accepted, but is independently verified server-side
// (exists, and activityId actually belongs to it) -- see ReviewService.
public record CreateReviewRequest(

        @NotNull
        UUID bookingId,

        @NotNull
        UUID businessId,

        @NotNull
        UUID activityId,

        @NotNull
        @Min(1)
        @Max(5)
        Integer rating,

        String title,

        @NotBlank
        String comment
) {
}
