package com.campuscourier.parcel.model;

/**
 * Delivery lifecycle for a parcel.
 * Beginner note: an enum is safer than a raw String —
 * the compiler stops you from saving a typo like "DELIVERD".
 */
public enum ParcelStatus {
    REGISTERED,
    ASSIGNED,
    IN_TRANSIT,
    DELIVERED,
    CANCELLED
}
