package com.campuscourier.parcel.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Light envelopes / documents. Cheapest handling:
 * flat base fee + small per-kg and per-km charges.
 */
@Entity
@DiscriminatorValue("DOCUMENT")
public class DocumentParcel extends Parcel {

    private static final double BASE_FEE = 500.0;
    private static final double PER_KG = 100.0;
    private static final double PER_KM = 20.0;

    protected DocumentParcel() {
        super();
    }

    public DocumentParcel(String senderName, String recipientName,
                          String pickupLocation, String dropoffLocation,
                          Double weightKg, Double distanceKm) {
        super(senderName, recipientName, pickupLocation, dropoffLocation, weightKg, distanceKm);
    }

    @Override
    public double calculateFee() {
        return BASE_FEE + (getWeightKg() * PER_KG) + (getDistanceKm() * PER_KM);
    }

    @Override
    public ParcelType getParcelType() {
        return ParcelType.DOCUMENT;
    }
}
