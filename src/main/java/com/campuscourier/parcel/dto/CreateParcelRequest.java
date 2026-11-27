package com.campuscourier.parcel.dto;

import com.campuscourier.parcel.model.ParcelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * JSON body for POST /api/v1/parcels.
 * Validation annotations run automatically because the
 * controller parameter is marked with @Valid.
 */
public class CreateParcelRequest {

    @NotNull(message = "parcelType is required (DOCUMENT, FRAGILE or BULK)")
    private ParcelType parcelType;

    @NotBlank(message = "senderName is required")
    private String senderName;

    @NotBlank(message = "recipientName is required")
    private String recipientName;

    @NotBlank(message = "pickupLocation is required")
    private String pickupLocation;

    @NotBlank(message = "dropoffLocation is required")
    private String dropoffLocation;

    @NotNull(message = "weightKg is required")
    @Positive(message = "weightKg must be greater than 0")
    private Double weightKg;

    @NotNull(message = "distanceKm is required")
    @Positive(message = "distanceKm must be greater than 0")
    private Double distanceKm;

    public ParcelType getParcelType() {
        return parcelType;
    }

    public void setParcelType(ParcelType parcelType) {
        this.parcelType = parcelType;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getDropoffLocation() {
        return dropoffLocation;
    }

    public void setDropoffLocation(String dropoffLocation) {
        this.dropoffLocation = dropoffLocation;
    }

    public Double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(Double weightKg) {
        this.weightKg = weightKg;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }
}
