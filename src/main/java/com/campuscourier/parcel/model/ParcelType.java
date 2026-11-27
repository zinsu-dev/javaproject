package com.campuscourier.parcel.model;

/**
 * The three parcel types the API accepts in POST /api/parcels.
 * Lives in the model package (not dto) so the Parcel entity itself can
 * report its type polymorphically without depending on API classes.
 */
public enum ParcelType {
    DOCUMENT,
    FRAGILE,
    BULK
}
