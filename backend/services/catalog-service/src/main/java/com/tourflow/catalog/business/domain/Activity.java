package com.tourflow.catalog.business.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "activities")
public class Activity {

    @Id
    private UUID id;

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(length = 255)
    private String location;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "max_participants", nullable = false)
    private Integer maxParticipants;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ActivityCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ActivityStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Activity() {
    }

    public Activity(
            UUID id,
            UUID businessId,
            String name,
            String description,
            String location,
            BigDecimal price,
            Integer durationMinutes,
            Integer maxParticipants,
            ActivityCategory category,
            ActivityStatus status,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.businessId = businessId;
        this.name = name;
        this.description = description;
        this.location = location;
        this.price = price;
        this.durationMinutes = durationMinutes;
        this.maxParticipants = maxParticipants;
        this.category = category;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getBusinessId() {
        return businessId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public ActivityCategory getCategory() {
        return category;
    }

    public ActivityStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setStatus(ActivityStatus status) {
        this.status = status;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Update the owner-editable fields. Status is deliberately not settable
    // here -- see activate/deactivate/delete in ActivityService.
    public void update(
            String name,
            String description,
            String location,
            BigDecimal price,
            Integer durationMinutes,
            Integer maxParticipants,
            ActivityCategory category
    ) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.price = price;
        this.durationMinutes = durationMinutes;
        this.maxParticipants = maxParticipants;
        this.category = category;
        this.updatedAt = OffsetDateTime.now();
    }
}
