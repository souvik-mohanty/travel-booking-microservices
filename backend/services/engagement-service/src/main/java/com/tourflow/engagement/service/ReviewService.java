package com.tourflow.review.service;

import com.tourflow.review.client.BookingClient;
import com.tourflow.review.client.BusinessClient;
import com.tourflow.review.domain.Review;
import com.tourflow.review.domain.ReviewStatus;
import com.tourflow.review.dto.CreateReviewRequest;
import com.tourflow.review.dto.ReviewResponse;
import com.tourflow.review.dto.UpdateReviewRequest;
import com.tourflow.review.exception.ReviewValidationException;
import com.tourflow.review.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingClient bookingClient;
    private final BusinessClient businessClient;

    public ReviewService(
            ReviewRepository reviewRepository,
            BookingClient bookingClient,
            BusinessClient businessClient
    ) {
        this.reviewRepository = reviewRepository;
        this.bookingClient = bookingClient;
        this.businessClient = businessClient;
    }

    public ReviewResponse createReview(
            UUID userId,
            CreateReviewRequest request
    ) {

        // 1. Prevent duplicate review.
        if (reviewRepository.existsByBookingId(request.bookingId())) {
            throw new ReviewValidationException("A review already exists for this booking");
        }

        // 2. Ask Booking Service for authoritative booking data.
        BookingClient.BookingInfo booking = bookingClient.getBooking(request.bookingId());

        // 3. Verify booking belongs to authenticated user.
        if (!booking.userId().equals(userId)) {
            throw new ReviewValidationException("You cannot review another user's booking");
        }

        // 4. Review allowed only after a completed booking.
        if (!"COMPLETED".equalsIgnoreCase(booking.status())) {
            throw new ReviewValidationException(
                    "Review can only be created after the booking is completed"
            );
        }

        // 5. Verify business exists.
        businessClient.verifyBusiness(request.businessId());

        // 6. Verify the activity belongs to that business.
        businessClient.verifyActivityBelongsToBusiness(request.businessId(), request.activityId());

        // Note: we still cannot prove the booking's tourId corresponds to
        // this activityId -- Booking Service and Business Service have no
        // established relationship yet. Not faking that check.

        OffsetDateTime now = OffsetDateTime.now();

        Review review = new Review(
                UUID.randomUUID(),
                booking.id(),
                request.activityId(),
                request.businessId(),
                userId,
                request.rating(),
                request.title(),
                request.comment(),
                ReviewStatus.ACTIVE,
                now,
                now
        );

        Review saved = reviewRepository.save(review);

        return ReviewResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public ReviewResponse getReview(UUID reviewId) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewValidationException("Review not found"));

        return ReviewResponse.fromEntity(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getMyReviews(UUID userId) {

        return reviewRepository.findByUserId(userId)
                .stream()
                .map(ReviewResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getBusinessReviews(UUID businessId) {

        return reviewRepository.findByBusinessIdAndStatus(businessId, ReviewStatus.ACTIVE)
                .stream()
                .map(ReviewResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getActivityReviews(UUID activityId) {

        return reviewRepository.findByActivityIdAndStatus(activityId, ReviewStatus.ACTIVE)
                .stream()
                .map(ReviewResponse::fromEntity)
                .toList();
    }

    public ReviewResponse updateReview(
            UUID userId,
            UUID reviewId,
            UpdateReviewRequest request
    ) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewValidationException("Review not found"));

        if (!review.getUserId().equals(userId)) {
            throw new ReviewValidationException("You can only update your own review");
        }

        if (request.rating() != null) {
            review.setRating(request.rating());
        }

        if (request.title() != null) {
            review.setTitle(request.title());
        }

        if (request.comment() != null) {
            review.setComment(request.comment());
        }

        review.setUpdatedAt(OffsetDateTime.now());

        return ReviewResponse.fromEntity(reviewRepository.save(review));
    }

    public void deleteReview(
            UUID userId,
            UUID reviewId
    ) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewValidationException("Review not found"));

        if (!review.getUserId().equals(userId)) {
            throw new ReviewValidationException("You can only delete your own review");
        }

        review.setStatus(ReviewStatus.DELETED);
        review.setUpdatedAt(OffsetDateTime.now());

        reviewRepository.save(review);
    }
}
