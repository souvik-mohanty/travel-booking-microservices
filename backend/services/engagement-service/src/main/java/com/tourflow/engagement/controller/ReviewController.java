package com.tourflow.engagement.controller;

import com.tourflow.engagement.dto.CreateReviewRequest;
import com.tourflow.engagement.dto.ReviewResponse;
import com.tourflow.engagement.dto.UpdateReviewRequest;
import com.tourflow.engagement.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// REST API for reviews. Reads are public except /me; creating, updating, and
// deleting a review require authentication -- see SecurityConfig.
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // Create a review for the authenticated user's completed booking.
    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @Valid @RequestBody CreateReviewRequest request,
            Authentication authentication
    ) {

        UUID userId = UUID.fromString(authentication.getName());

        ReviewResponse response = reviewService.createReview(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Return a single review by ID.
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> getReview(
            @PathVariable UUID reviewId
    ) {

        return ResponseEntity.ok(
                reviewService.getReview(reviewId)
        );
    }

    // Return all reviews written by the authenticated user.
    @GetMapping("/me")
    public ResponseEntity<List<ReviewResponse>> getMyReviews(
            Authentication authentication
    ) {

        UUID userId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                reviewService.getMyReviews(userId)
        );
    }

    // Return all active reviews for a business.
    @GetMapping("/business/{businessId}")
    public ResponseEntity<List<ReviewResponse>> getBusinessReviews(
            @PathVariable UUID businessId
    ) {

        return ResponseEntity.ok(
                reviewService.getBusinessReviews(businessId)
        );
    }

    // Return all active reviews for an activity.
    @GetMapping("/activity/{activityId}")
    public ResponseEntity<List<ReviewResponse>> getActivityReviews(
            @PathVariable UUID activityId
    ) {

        return ResponseEntity.ok(
                reviewService.getActivityReviews(activityId)
        );
    }

    // Update the authenticated user's own review.
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable UUID reviewId,
            @Valid @RequestBody UpdateReviewRequest request,
            Authentication authentication
    ) {

        UUID userId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                reviewService.updateReview(userId, reviewId, request)
        );
    }

    // Soft-delete the authenticated user's own review.
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable UUID reviewId,
            Authentication authentication
    ) {

        UUID userId = UUID.fromString(authentication.getName());

        reviewService.deleteReview(userId, reviewId);

        return ResponseEntity.noContent().build();
    }
}
