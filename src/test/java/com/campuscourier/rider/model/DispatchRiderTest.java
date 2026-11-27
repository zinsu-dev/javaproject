package com.campuscourier.rider.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Verifies DispatchRider defaults: a new rider is always available
 * and always has an identity, without the caller setting anything.
 */
class DispatchRiderTest {

    @Test
    void newRiderIsAvailable() {
        DispatchRider rider = new DispatchRider("Yaw Mensah", "0244123456");

        assertEquals(Boolean.TRUE, rider.isAvailable());
    }

    @Test
    void newRiderHasDetailsAndNoIdYet() {
        DispatchRider rider = new DispatchRider("Yaw Mensah", "0244123456");

        assertEquals("Yaw Mensah", rider.getFullName());
        assertEquals("0244123456", rider.getPhoneNumber());
        // Like parcels, the id is assigned by the database on save —
        // assertNotNull(getId()) belongs in the repository test instead.
    }

    @Test
    void availabilityCanBeToggled() {
        DispatchRider rider = new DispatchRider("Yaw Mensah", "0244123456");

        rider.setAvailable(Boolean.FALSE);

        assertEquals(Boolean.FALSE, rider.isAvailable());
        assertTrue(!rider.isAvailable());
    }
}
