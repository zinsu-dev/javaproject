package com.campuscourier.parcel.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Breakable items (glass, electronics...). Costs more because of
 * special packaging + careful handling surcharge.
 */
@Entity
@DiscriminatorValue("FRAGILE")
public class FragileParcel extends Parcel {

    private static final double BASE_FEE = 1500.0;
    private static final double PER_KG = 300.0;
    private static final double PER_KM = 50.0;

    protected FragileParcel() {
        super();
    }

    public FragileParcel(String senderName, String recipientName,
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
        return ParcelType.FRAGILE;
    }
}
