package com.campuscourier.parcel.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.math.BigDecimal;

/**
 * Light envelopes / documents. Cheapest handling: flat ₦500.
 * Stored in the shared "parcels" table with parcel_type = 'DOCUMENT'.
 *
 * Note the String constructor argument: new BigDecimal("500") is exact,
 * while new BigDecimal(500.0) would inherit binary floating-point error.
 */
@Entity
@DiscriminatorValue("DOCUMENT")
public class DocumentParcel extends Parcel {

    private static final BigDecimal HANDLING_FEE = new BigDecimal("500");

    /** For Hibernate only. Do not use in application code. */
    protected DocumentParcel() {
        super();
    }

    public DocumentParcel(String senderName, String recipientName,
                          String pickupLocation, String destination,
                          BigDecimal weight) {
        super(senderName, recipientName, pickupLocation, destination, weight);
    }

    @Override
    public BigDecimal calculateHandlingFee() {
        return HANDLING_FEE;
    }
}
