package com.campuscourier.parcel.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.math.BigDecimal;

/**
 * Heavy / large freight (hostel move-out boxes, department equipment...).
 * Needs a vehicle and possibly extra hands: flat ₦3,000.
 * Stored in the shared "parcels" table with parcel_type = 'BULK'.
 */
@Entity
@DiscriminatorValue("BULK")
public class BulkFreight extends Parcel {

    private static final BigDecimal HANDLING_FEE = new BigDecimal("3000");

    /** For Hibernate only. Do not use in application code. */
    protected BulkFreight() {
        super();
    }

    public BulkFreight(String senderName, String recipientName,
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
        return ParcelType.BULK;
    }
}
