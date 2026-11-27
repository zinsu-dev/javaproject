package com.campuscourier.parcel.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Base parcel entity.
 *
 * ARCHITECTURE DECISION (why this looks this way):
 * - One table for all parcel types (SINGLE_TABLE inheritance).
 *   Simple for an MVP: one repository, one query, no joins.
 *   The "parcel_type" column tells Hibernate which subclass to build.
 * - The fee method is ABSTRACT: every subclass MUST provide its own
 *   formula. Calling parcel.calculateFee() runs the subclass version
 *   at runtime — that is runtime polymorphism.
 * - Entities are never returned directly from the API. The service
 *   converts them to DTOs (see dto package) so database details
 *   don't leak into JSON.
 */
@Entity
@Table(name = "parcels")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "parcel_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Parcel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tracking_number", unique = true, nullable = false, updatable = false)
    private String trackingNumber;

    @Column(nullable = false)
    private String senderName;

    @Column(nullable = false)
    private String recipientName;

    @Column(nullable = false)
    private String pickupLocation;

    @Column(nullable = false)
    private String dropoffLocation;

    @Column(nullable = false)
    private Double weightKg;

    @Column(nullable = false)
    private Double distanceKm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParcelStatus status = ParcelStatus.REGISTERED;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected Parcel() {
        // Required by JPA. Never call directly — use subclass constructors.
    }

    protected Parcel(String senderName, String recipientName,
                     String pickupLocation, String dropoffLocation,
                     Double weightKg, Double distanceKm) {
        this.senderName = senderName;
        this.recipientName = recipientName;
        this.pickupLocation = pickupLocation;
        this.dropoffLocation = dropoffLocation;
        this.weightKg = weightKg;
        this.distanceKm = distanceKm;
        this.status = ParcelStatus.REGISTERED;
    }

    /**
     * Each parcel type implements its own handling-fee formula.
     * The service/controller just call this method — they don't need
     * if/else chains checking the parcel type.
     */
    public abstract double calculateFee();

    /** Each subclass reports its type so DTOs stay simple. */
    public abstract ParcelType getParcelType();

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // --- Getters and setters (explicit on purpose: easier for beginners
    //     to see what is happening than Lombok magic) ---

    public Long getId() {
        return id;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getDropoffLocation() {
        return dropoffLocation;
    }

    public void setDropoffLocation(String dropoffLocation) {
        this.dropoffLocation = dropoffLocation;
    }

    public Double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(Double weightKg) {
        this.weightKg = weightKg;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public ParcelStatus getStatus() {
        return status;
    }

    public void setStatus(ParcelStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
