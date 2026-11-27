package com.campuscourier.rider.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    void newRiderHasGeneratedIdAndDetails() {
        DispatchRider rider = new DispatchRider("Yaw Mensah", "0244123456");

        assertNotNull(rider.getId());
        assertEquals("Yaw Mensah", rider.getFullName());
        assertEquals("0244123456", rider.getPhoneNumber());
    }

    @Test
    void availabilityCanBeToggled() {
        DispatchRider rider = new DispatchRider("Yaw Mensah", "0244123456");

        rider.setAvailable(Boolean.FALSE);

        assertEquals(Boolean.FALSE, rider.isAvailable());
        assertTrue(!rider.isAvailable());
    }
}
