package com.campuscourier.parcel.model;

/**
 * The three parcel types the API accepts when creating a parcel.
 * Beginner note: this is the "type" field in the JSON request.
 * The service uses it to decide which subclass to create.
 */
public enum ParcelType {
    DOCUMENT,
    FRAGILE,
    BULK
}
