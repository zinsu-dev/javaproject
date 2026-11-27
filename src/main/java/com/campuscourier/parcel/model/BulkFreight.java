package com.campuscourier.parcel.model;

import java.math.BigDecimal;

/**
 * Heavy / large freight (hostel move-out boxes, department equipment...).
 * Needs a vehicle and possibly extra hands: flat ₦3,000.
 */
public class BulkFreight extends Parcel {

    private static final BigDecimal HANDLING_FEE = new BigDecimal("3000");

    public BulkFreight(String senderName, String recipientName,
                       String pickupLocation, String destination,
                       BigDecimal weight) {
        super(senderName, recipientName, pickupLocation, destination, weight);
    }

    @Override
    public BigDecimal calculateHandlingFee() {
        return HANDLING_FEE;
    }
}
