package com.campuscourier.parcel.dto;

import com.campuscourier.parcel.model.Parcel;
import com.campuscourier.parcel.model.ParcelStatus;
import com.campuscourier.parcel.model.ParcelType;
import java.time.LocalDateTime;

/**
 * JSON returned by the API. Built from the entity so we can include
 * the computed fee without storing it in the database.
 */
public class ParcelResponse {

    private Long id;
    private String trackingNumber;
    private ParcelType parcelType;
    private String senderName;
    private String recipientName;
    private String pickupLocation;
    private String dropoffLocation;
    private Double weightKg;
    private Double distanceKm;
    private ParcelStatus status;
    private double fee;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ParcelResponse from(Parcel parcel) {
        ParcelResponse dto = new ParcelResponse();
        dto.id = parcel.getId();
        dto.trackingNumber = parcel.getTrackingNumber();
        dto.parcelType = parcel.getParcelType();
        dto.senderName = parcel.getSenderName();
        dto.recipientName = parcel.getRecipientName();
        dto.pickupLocation = parcel.getPickupLocation();
        dto.dropoffLocation = parcel.getDropoffLocation();
        dto.weightKg = parcel.getWeightKg();
        dto.distanceKm = parcel.getDistanceKm();
        dto.status = parcel.getStatus();
        dto.fee = parcel.calculateFee(); // <-- polymorphism in action
        dto.createdAt = parcel.getCreatedAt();
        dto.updatedAt = parcel.getUpdatedAt();
        return dto;
    }

    public Long getId() {
        return id;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public ParcelType getParcelType() {
        return parcelType;
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

    public String getDropoffLocation() {
        return dropoffLocation;
    }

    public Double getWeightKg() {
        return weightKg;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public ParcelStatus getStatus() {
        return status;
    }

    public double getFee() {
        return fee;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
