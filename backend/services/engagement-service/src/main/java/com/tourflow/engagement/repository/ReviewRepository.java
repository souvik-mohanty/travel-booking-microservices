package com.tourflow.engagement.repository;

import com.tourflow.engagement.domain.Review;
import com.tourflow.engagement.domain.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    boolean existsByBookingId(UUID bookingId);

    List<Review> findByUserId(UUID userId);

    List<Review> findByActivityIdAndStatus(UUID activityId, ReviewStatus status);

    List<Review> findByBusinessIdAndStatus(UUID businessId, ReviewStatus status);
}
