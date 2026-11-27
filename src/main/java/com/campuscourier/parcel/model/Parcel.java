package com.campuscourier.parcel.model;

import com.campuscourier.rider.model.DispatchRider;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Base parcel entity.
 *
 * PERSISTENCE NOTES (for beginners):
 * - SINGLE_TABLE inheritance = ONE "parcels" table for all three parcel
 *   types. The "parcel_type" column tells Hibernate which subclass to
 *   build when reading a row back ("DOCUMENT", "FRAGILE" or "BULK").
 * - All business logic from Phase 1 is untouched: the abstract
 *   calculateHandlingFee(), calculateTotalFee(), validation, PENDING
 *   default. JPA annotations only describe the database mapping.
 * - The protected no-arg constructor exists ONLY for Hibernate (it
 *   creates empty objects via reflection, then fills fields). Always
 *   use the full constructor in application code.
 */
@Entity
@Table(name = "parcels")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "parcel_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Parcel {

    /**
     * Database-generated identity. NULL until the parcel is saved for the
     * first time — GenerationType.UUID asks Hibernate to fill it in on
     * persist. (The human-facing code is trackingId, generated below.)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tracking_id", nullable = false, unique = true, updatable = false)
    private String trackingId;

    @Column(name = "sender_name", nullable = false)
    private String senderName;

    @Column(name = "recipient_name", nullable = false)
    private String recipientName;

    @Column(name = "pickup_location", nullable = false)
    private String pickupLocation;

    @Column(name = "destination", nullable = false)
    private String destination;

    @Column(name = "weight", nullable = false, precision = 10, scale = 3)
    private BigDecimal weight;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DeliveryStatus status;

    /**
     * The rider carrying this parcel. Optional: a parcel exists first
     * (PENDING) and is assigned a rider later. Many parcels can point
     * to the same rider over time. LAZY = the rider row is only loaded
     * when getRider() is actually called, not with every parcel query.
     * Unidirectional on purpose: the rider does not hold a parcel list,
     * which avoids circular references and huge accidental data loads.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rider_id")
    private DispatchRider rider;

    /** For Hibernate only. Do not use in application code. */
    protected Parcel() {
        this.status = DeliveryStatus.PENDING;
    }

    protected Parcel(String senderName, String recipientName,
                     String pickupLocation, String destination,
                     BigDecimal weight) {
        this.trackingId = generateTrackingId();
        this.senderName = requireText(senderName, "senderName");
        this.recipientName = requireText(recipientName, "recipientName");
        this.pickupLocation = requireText(pickupLocation, "pickupLocation");
        this.destination = requireText(destination, "destination");
        setWeight(weight); // reuses the validation in one place
        this.status = DeliveryStatus.PENDING;
    }

    /**
     * Each parcel type defines its OWN handling fee.
     * Abstract = the parent declares WHAT is needed, each subclass
     * decides HOW. Calling this on a Parcel reference runs the
     * subclass version at runtime (runtime polymorphism).
     *
     * @return handling fee in Naira, never null
     */
    public abstract BigDecimal calculateHandlingFee();

    /**
     * Reports which parcel type this is (DOCUMENT, FRAGILE or BULK).
     * Like calculateHandlingFee(), each subclass answers for itself —
     * callers never need instanceof checks.
     */
    public abstract ParcelType getParcelType();

    /**
     * Total amount the customer pays. Right now that is just the
     * handling fee; later phases (distance surcharge, insurance) will
     * extend THIS method, so subclasses never need to change.
     *
     * @return total fee in Naira, never null
     */
    public BigDecimal calculateTotalFee() {
        return calculateHandlingFee();
    }

    private static String generateTrackingId() {
        return "CC-" + UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    private static void requirePositiveWeight(BigDecimal weight) {
        // BigDecimal has no < operator, so we use compareTo:
        // compareTo returns -1, 0, 1 for less/equal/greater.
        if (weight == null || weight.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Parcel weight must be greater than zero");
        }
    }

    // ---- Accessors. Fields stay private (encapsulation): the only way
    // ---- to read/change state is through these methods, so validation
    // ---- in setWeight() can never be bypassed. id has no setter because
    // ---- identity must never change after creation.

    public UUID getId() {
        return id;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = requireText(trackingId, "trackingId");
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = requireText(senderName, "senderName");
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = requireText(recipientName, "recipientName");
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = requireText(pickupLocation, "pickupLocation");
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = requireText(destination, "destination");
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        requirePositiveWeight(weight);
        this.weight = weight;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public void setStatus(DeliveryStatus status) {
        this.status = Objects.requireNonNull(status, "status must not be null");
    }

    public DispatchRider getRider() {
        return rider;
    }

    public void setRider(DispatchRider rider) {
        this.rider = rider;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "{trackingId='" + trackingId
                + "', status=" + status + "}";
    }
}
