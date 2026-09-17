package com.tourflow.driver.service;

import com.tourflow.driver.domain.Driver;
import com.tourflow.driver.domain.DriverAvailability;
import com.tourflow.driver.domain.DriverStatus;
import com.tourflow.driver.dto.CreateDriverRequest;
import com.tourflow.driver.dto.DriverResponse;
import com.tourflow.driver.exception.DriverAccessDeniedException;
import com.tourflow.driver.exception.DriverAlreadyExistsException;
import com.tourflow.driver.exception.DriverNotFoundException;
import com.tourflow.driver.repository.DriverRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class DriverService {

    private final DriverRepository driverRepository;

    public DriverService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    // Register the authenticated user as a driver. One driver profile per user.
    @Transactional
    public DriverResponse createDriver(
            CreateDriverRequest request,
            UUID userId
    ) {

        if (driverRepository.existsByUserId(userId)) {
            throw new DriverAlreadyExistsException(
                    "A driver profile already exists for this user"
            );
        }

        OffsetDateTime now = OffsetDateTime.now();

        Driver driver = new Driver(
                UUID.randomUUID(),
                userId,
                request.licenseNumber(),
                request.licenseExpiryDate(),
                request.phone(),

                // No verifier role exists yet -- see DriverStatus.
                DriverStatus.PENDING_VERIFICATION,

                DriverAvailability.UNAVAILABLE,
                now,
                now
        );

        try {
            driver = driverRepository.saveAndFlush(driver);
        } catch (DataIntegrityViolationException ex) {
            throw new DriverAlreadyExistsException(
                    "A driver profile already exists for this user"
            );
        }

        return DriverResponse.fromEntity(driver);
    }

    @Transactional(readOnly = true)
    public DriverResponse getDriver(UUID id) {

        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new DriverNotFoundException("Driver not found"));

        return DriverResponse.fromEntity(driver);
    }

    @Transactional(readOnly = true)
    public DriverResponse getMyDriverProfile(UUID userId) {

        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new DriverNotFoundException("You do not have a driver profile yet")
                );

        return DriverResponse.fromEntity(driver);
    }

    // Toggle the authenticated driver's own availability.
    @Transactional
    public DriverResponse setAvailability(
            UUID driverId,
            UUID userId,
            DriverAvailability availability
    ) {

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new DriverNotFoundException("Driver not found"));

        if (!driver.getUserId().equals(userId)) {
            throw new DriverAccessDeniedException(
                    "You cannot change another driver's availability"
            );
        }

        if (driver.getAvailability() == availability) {
            return DriverResponse.fromEntity(driver);
        }

        driver.setAvailability(availability);
        driver.setUpdatedAt(OffsetDateTime.now());

        return DriverResponse.fromEntity(driverRepository.save(driver));
    }
}
