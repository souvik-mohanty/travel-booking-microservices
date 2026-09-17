package com.tourflow.driver.dto;

import com.tourflow.driver.domain.Driver;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record DriverResponse(
        UUID id,
        UUID userId,
        String licenseNumber,
        LocalDate licenseExpiryDate,
        String phone,
        String status,
        String availability,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static DriverResponse fromEntity(Driver driver) {
        return new DriverResponse(
                driver.getId(),
                driver.getUserId(),
                driver.getLicenseNumber(),
                driver.getLicenseExpiryDate(),
                driver.getPhone(),
                driver.getStatus().name(),
                driver.getAvailability().name(),
                driver.getCreatedAt(),
                driver.getUpdatedAt()
        );
    }
}
