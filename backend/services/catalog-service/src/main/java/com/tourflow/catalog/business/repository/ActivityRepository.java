package com.tourflow.catalog.business.repository;

import com.tourflow.catalog.business.domain.Activity;
import com.tourflow.catalog.business.domain.ActivityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ActivityRepository extends JpaRepository<Activity, UUID> {

    List<Activity> findByBusinessId(UUID businessId);

    List<Activity> findByStatus(ActivityStatus status);
}
