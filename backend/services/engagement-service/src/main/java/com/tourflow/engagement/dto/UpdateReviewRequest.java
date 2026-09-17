package com.tourflow.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

// Partial update: a null field means "leave unchanged" -- see ReviewService.updateReview.
public record UpdateReviewRequest(

        @Min(1)
        @Max(5)
        Integer rating,

        String title,

        String comment
) {
}
