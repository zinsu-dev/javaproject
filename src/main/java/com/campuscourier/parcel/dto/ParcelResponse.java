package com.campuscourier.parcel.dto;

import com.campuscourier.parcel.model.DeliveryStatus;
import com.campuscourier.parcel.model.Parcel;
import com.campuscourier.parcel.model.ParcelType;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * JSON returned by every parcel endpoint.
 *
 * Built from the entity (never the entity itself): this keeps lazy-loaded
 * relationships like rider out of the JSON, freezes the API shape even if
 * the database mapping changes, and lets us include computed values
 * (handlingFee, totalFee) without storing them.
 * BigDecimal serializes as a JSON number, e.g. "handlingFee": 500.
 */
public class ParcelResponse {

    private UUID id;
    private String trackingId;
    private String senderName;
    private String recipientName;
    private String pickupLocation;
    private String destination;
    private BigDecimal weight;
    private ParcelType parcelType;
    private DeliveryStatus status;
    private BigDecimal handlingFee;
    private BigDecimal totalFee;

    public static ParcelResponse from(Parcel parcel) {
        ParcelResponse dto = new ParcelResponse();
        dto.id = parcel.getId();
        dto.trackingId = parcel.getTrackingId();
        dto.senderName = parcel.getSenderName();
        dto.recipientName = parcel.getRecipientName();
        dto.pickupLocation = parcel.getPickupLocation();
        dto.destination = parcel.getDestination();
        dto.weight = parcel.getWeight();
        dto.parcelType = parcel.getParcelType(); // polymorphic, no instanceof
        dto.status = parcel.getStatus();
        dto.handlingFee = parcel.calculateHandlingFee(); // polymorphic
        dto.totalFee = parcel.calculateTotalFee();
        // NOTE: parcel.getRider() is deliberately NOT touched — reading it
        // would trigger a lazy database load during JSON rendering.
        return dto;
    }

    public UUID getId() {
        return id;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public String getDestination() {
        return destination;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public ParcelType getParcelType() {
        return parcelType;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public BigDecimal getHandlingFee() {
        return handlingFee;
    }

    public BigDecimal getTotalFee() {
        return totalFee;
    }
}
