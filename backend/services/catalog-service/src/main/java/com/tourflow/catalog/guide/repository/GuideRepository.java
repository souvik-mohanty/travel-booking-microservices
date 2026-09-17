package com.tourflow.guide.repository;

import com.tourflow.guide.domain.Guide;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GuideRepository extends JpaRepository<Guide, UUID> {

    Optional<Guide> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);
}
