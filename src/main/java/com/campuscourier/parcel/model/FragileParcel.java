package com.campuscourier.parcel.model;

import java.math.BigDecimal;

/**
 * Breakable items (glass, electronics...). Special packaging and careful
 * handling cost extra: flat ₦1,500.
 */
public class FragileParcel extends Parcel {

    private static final BigDecimal HANDLING_FEE = new BigDecimal("1500");

    public FragileParcel(String senderName, String recipientName,
                         String pickupLocation, String destination,
                         BigDecimal weight) {
        super(senderName, recipientName, pickupLocation, destination, weight);
    }

    @Override
    public BigDecimal calculateHandlingFee() {
        return HANDLING_FEE;
    }
}
