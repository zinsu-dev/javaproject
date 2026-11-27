package com.campuscourier.parcel.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.math.BigDecimal;

/**
 * Breakable items (glass, electronics...). Special packaging and careful
 * handling cost extra: flat ₦1,500.
 * Stored in the shared "parcels" table with parcel_type = 'FRAGILE'.
 */
@Entity
@DiscriminatorValue("FRAGILE")
public class FragileParcel extends Parcel {

    private static final BigDecimal HANDLING_FEE = new BigDecimal("1500");

    /** For Hibernate only. Do not use in application code. */
    protected FragileParcel() {
        super();
    }

    public FragileParcel(String senderName, String recipientName,
                         String pickupLocation, String destination,
                         BigDecimal weight) {
        super(senderName, recipientName, pickupLocation, destination, weight);
    }

    @Override
    public BigDecimal calculateHandlingFee() {
        return HANDLING_FEE;
    }

    @Override
    public ParcelType getParcelType() {
        return ParcelType.FRAGILE;
    }
}
