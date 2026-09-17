package com.tourflow.review.repository;

import com.tourflow.review.domain.Review;
import com.tourflow.review.domain.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    boolean existsByBookingId(UUID bookingId);

    List<Review> findByUserId(UUID userId);

    List<Review> findByActivityIdAndStatus(UUID activityId, ReviewStatus status);

    List<Review> findByBusinessIdAndStatus(UUID businessId, ReviewStatus status);
}
