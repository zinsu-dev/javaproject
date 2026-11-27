package com.campuscourier.parcel.model;

import java.math.BigDecimal;

/**
 * Light envelopes / documents. Cheapest handling: flat ₦500.
 *
 * Note the String constructor argument: new BigDecimal("500") is exact,
 * while new BigDecimal(500.0) would inherit binary floating-point error.
 */
public class DocumentParcel extends Parcel {

    private static final BigDecimal HANDLING_FEE = new BigDecimal("500");

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
