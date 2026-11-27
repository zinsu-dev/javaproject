package com.campuscourier.parcel.factory;

import com.campuscourier.parcel.model.BulkFreight;
import com.campuscourier.parcel.model.DocumentParcel;
import com.campuscourier.parcel.model.FragileParcel;
import com.campuscourier.parcel.model.Parcel;
import com.campuscourier.parcel.model.ParcelType;
import java.math.BigDecimal;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * Builds the correct Parcel subclass from a ParcelType.
 *
 * WHY A SEPARATE CLASS: object creation is a different responsibility
 * from business logic. The service asks "make me a parcel of this type"
 * and stays free of construction details; if a fourth type (e.g.
 * ExpressParcel) is added later, only this switch grows.
 * NOTE: the switch picks the CLASS — fee math still lives in each
 * subclass's calculateHandlingFee(), never in if/else chains here.
 */
@Component
public class ParcelFactory {

    public Parcel create(ParcelType parcelType,
                         String senderName,
                         String recipientName,
                         String pickupLocation,
                         String destination,
                         BigDecimal weight) {
        Objects.requireNonNull(parcelType, "parcelType must not be null");
        return switch (parcelType) {
            case DOCUMENT -> new DocumentParcel(
                    senderName, recipientName, pickupLocation, destination, weight);
            case FRAGILE -> new FragileParcel(
                    senderName, recipientName, pickupLocation, destination, weight);
            case BULK -> new BulkFreight(
                    senderName, recipientName, pickupLocation, destination, weight);
        };
    }
}
