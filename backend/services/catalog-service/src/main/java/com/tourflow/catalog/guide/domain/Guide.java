package com.tourflow.catalog.guide.domain;

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
        name = "guides",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_guides_user", columnNames = "user_id")
        }
)
public class Guide {

    @Id
    private UUID id;

    // identity-service user this guide profile belongs to.
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(length = 2000)
    private String bio;

    @Column(name = "languages_spoken", length = 255)
    private String languagesSpoken;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Column(length = 30)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private GuideStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GuideAvailability availability;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Guide() {
    }

    public Guide(
            UUID id,
            UUID userId,
            String bio,
            String languagesSpoken,
            Integer yearsOfExperience,
            String phone,
            GuideStatus status,
            GuideAvailability availability,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.bio = bio;
        this.languagesSpoken = languagesSpoken;
        this.yearsOfExperience = yearsOfExperience;
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

    public String getBio() {
        return bio;
    }

    public String getLanguagesSpoken() {
        return languagesSpoken;
    }

    public Integer getYearsOfExperience() {
        return yearsOfExperience;
    }

    public String getPhone() {
        return phone;
    }

    public GuideStatus getStatus() {
        return status;
    }

    public GuideAvailability getAvailability() {
        return availability;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setAvailability(GuideAvailability availability) {
        this.availability = availability;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
