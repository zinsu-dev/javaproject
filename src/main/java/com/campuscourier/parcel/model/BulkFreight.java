package com.campuscourier.parcel.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Heavy / large freight (hostel move-out boxes, department equipment...).
 * Highest rates: needs a vehicle and possibly extra hands.
 */
@Entity
@DiscriminatorValue("BULK")
public class BulkFreight extends Parcel {

    private static final double BASE_FEE = 3000.0;
    private static final double PER_KG = 500.0;
    private static final double PER_KM = 100.0;

    protected BulkFreight() {
        super();
    }

    public BulkFreight(String senderName, String recipientName,
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
        return ParcelType.BULK;
    }
}
