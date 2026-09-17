package com.tourflow.mobility.fleet.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "vehicles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_vehicles_registration", columnNames = "registration_number")
        }
)
public class Vehicle {

    @Id
    private UUID id;

    // identity-service user who manages this vehicle (the fleet manager).
    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "registration_number", nullable = false, length = 30)
    private String registrationNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VehicleType type;

    @Column(nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VehicleStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Vehicle() {
    }

    public Vehicle(
            UUID id,
            UUID ownerId,
            String registrationNumber,
            VehicleType type,
            Integer capacity,
            VehicleStatus status,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.ownerId = ownerId;
        this.registrationNumber = registrationNumber;
        this.type = type;
        this.capacity = capacity;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public VehicleType getType() {
        return type;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Update the owner-editable fields. Status is deliberately not settable
    // here -- see FleetService.setStatus.
    public void update(VehicleType type, Integer capacity) {
        this.type = type;
        this.capacity = capacity;
        this.updatedAt = OffsetDateTime.now();
    }
}
