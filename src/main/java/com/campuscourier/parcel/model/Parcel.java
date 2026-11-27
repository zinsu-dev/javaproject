package com.campuscourier.parcel.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Base class for every parcel in the Campus Courier system.
 *
 * DESIGN NOTES (for beginners):
 * - This class is ABSTRACT: you can never do "new Parcel(...)". It only
 *   exists to hold behaviour shared by all parcel types. Concrete types
 *   (DocumentParcel, FragileParcel, BulkFreight) extend it.
 * - It is a plain Java class on purpose: no database annotations yet.
 *   Persistence (JPA) arrives in Phase 2, so this model can be tested
 *   as pure Java with zero infrastructure.
 * - Money uses BigDecimal, never double (see calculateHandlingFee docs).
 */
public abstract class Parcel {

    /** Unique identity, generated once and never changed. */
    private final UUID id;

    /** Human-friendly code given to the sender, e.g. "CC-3F9A2C1D". */
    private String trackingId;

    private String senderName;
    private String recipientName;
    private String pickupLocation;
    private String destination;

    /** Weight in kilograms. Must always be greater than zero. */
    private BigDecimal weight;

    /** Every new parcel starts life as PENDING. */
    private DeliveryStatus status;

    /**
     * @param senderName    who sends the parcel (must not be null/blank)
     * @param recipientName who receives it (must not be null/blank)
     * @param pickupLocation where the rider collects it
     * @param destination    where it must be delivered
     * @param weight         weight in kg, must be greater than zero
     */
    protected Parcel(String senderName, String recipientName,
                     String pickupLocation, String destination,
                     BigDecimal weight) {
        this.id = UUID.randomUUID();
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

    public final void setWeight(BigDecimal weight) {
        requirePositiveWeight(weight);
        this.weight = weight;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public void setStatus(DeliveryStatus status) {
        this.status = Objects.requireNonNull(status, "status must not be null");
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "{trackingId='" + trackingId
                + "', status=" + status + "}";
    }
}
