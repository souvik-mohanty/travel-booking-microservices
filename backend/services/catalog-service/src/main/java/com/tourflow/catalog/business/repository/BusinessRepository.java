package com.tourflow.catalog.business.repository;

import com.tourflow.catalog.business.domain.Business;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BusinessRepository extends JpaRepository<Business, UUID> {

    List<Business> findByOwnerId(UUID ownerId);
}
