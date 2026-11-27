package com.campuscourier.parcel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * JSON body for PUT /api/parcels/{id}.
 *
 * Only the sender-editable delivery details can change here. Status
 * transitions and rider assignment belong to later phases (dispatch
 * workflow / status history), so they are intentionally absent —
 * clients cannot smuggle them in through this endpoint.
 */
public class UpdateParcelRequest {

    @NotBlank(message = "senderName must not be blank")
    private String senderName;

    @NotBlank(message = "recipientName must not be blank")
    private String recipientName;

    @NotBlank(message = "pickupLocation must not be blank")
    private String pickupLocation;

    @NotBlank(message = "destination must not be blank")
    private String destination;

    @NotNull(message = "weight is required")
    @Positive(message = "weight must be greater than zero")
    private BigDecimal weight;

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

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }
}
