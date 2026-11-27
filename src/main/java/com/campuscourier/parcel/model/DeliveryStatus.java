package com.campuscourier.parcel.model;

/**
 * Delivery lifecycle of a parcel.
 *
 * An enum (instead of a raw String) guarantees only valid states exist —
 * the compiler rejects typos like "DELIVERD", and switch statements
 * over this type are checked for completeness.
 */
public enum DeliveryStatus {
    /** Parcel registered, waiting to be assigned to a rider. */
    PENDING,
    /** A dispatch rider has been assigned. */
    ASSIGNED,
    /** Rider has collected the parcel from the sender. */
    PICKED_UP,
    /** Parcel is on the way to the destination. */
    IN_TRANSIT,
    /** Parcel handed over to the recipient. */
    DELIVERED,
    /** Registration cancelled before delivery. */
    CANCELLED
}
