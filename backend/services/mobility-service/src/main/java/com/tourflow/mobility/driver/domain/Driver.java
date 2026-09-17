package com.tourflow.mobility.driver.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "drivers",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_drivers_user", columnNames = "user_id")
        }
)
public class Driver {

    @Id
    private UUID id;

    // identity-service user this driver profile belongs to.
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "license_number", nullable = false, length = 50)
    private String licenseNumber;

    @Column(name = "license_expiry_date", nullable = false)
    private LocalDate licenseExpiryDate;

    @Column(length = 30)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DriverStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DriverAvailability availability;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Driver() {
    }

    public Driver(
            UUID id,
            UUID userId,
            String licenseNumber,
            LocalDate licenseExpiryDate,
            String phone,
            DriverStatus status,
            DriverAvailability availability,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.licenseNumber = licenseNumber;
        this.licenseExpiryDate = licenseExpiryDate;
        this.phone = phone;
        this.status = status;
        this.availability = availability;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public LocalDate getLicenseExpiryDate() {
        return licenseExpiryDate;
    }

    public String getPhone() {
        return phone;
    }

    public DriverStatus getStatus() {
        return status;
    }

    public DriverAvailability getAvailability() {
        return availability;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setAvailability(DriverAvailability availability) {
        this.availability = availability;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
