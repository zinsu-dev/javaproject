package com.campuscourier.parcel.dto;

import com.campuscourier.parcel.model.ParcelStatus;
import jakarta.validation.constraints.NotNull;

/**
 * JSON body for PATCH /api/v1/parcels/{id}/status
 * Example: { "status": "IN_TRANSIT" }
 */
public class UpdateStatusRequest {

    @NotNull(message = "status is required")
    private ParcelStatus status;

    public ParcelStatus getStatus() {
        return status;
    }

    public void setStatus(ParcelStatus status) {
        this.status = status;
    }
}
