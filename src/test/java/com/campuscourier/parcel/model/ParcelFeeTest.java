package com.campuscourier.parcel.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Proves runtime polymorphism: the SAME method call
 * (parcel.calculateFee()) runs DIFFERENT code per subclass.
 * Pure unit test — no database, no Spring, runs in milliseconds.
 */
class ParcelFeeTest {

    @Test
    void documentParcelUsesCheapestFormula() {
        Parcel parcel = new DocumentParcel("Ama", "Kofi", "Hall A", "Library", 1.0, 2.0);
        // 500 + 1*100 + 2*20 = 640
        assertEquals(640.0, parcel.calculateFee(), 0.001);
    }

    @Test
    void fragileParcelAddsHandlingSurcharge() {
        Parcel parcel = new FragileParcel("Ama", "Kofi", "Hall A", "Library", 1.0, 2.0);
        // 1500 + 1*300 + 2*50 = 1900
        assertEquals(1900.0, parcel.calculateFee(), 0.001);
    }

    @Test
    void bulkFreightIsMostExpensive() {
        Parcel parcel = new BulkFreight("Dept", "Store", "Gate", "Faculty", 10.0, 5.0);
        // 3000 + 10*500 + 5*100 = 8500
        assertEquals(8500.0, parcel.calculateFee(), 0.001);
    }

    @Test
    void sameCallDifferentResultsProvesPolymorphism() {
        Parcel doc = new DocumentParcel("A", "B", "X", "Y", 2.0, 3.0);
        Parcel fragile = new FragileParcel("A", "B", "X", "Y", 2.0, 3.0);
        Parcel bulk = new BulkFreight("A", "B", "X", "Y", 2.0, 3.0);

        // Identical inputs, three different fees — decided at RUNTIME.
        assertTrue(doc.calculateFee() < fragile.calculateFee());
        assertTrue(fragile.calculateFee() < bulk.calculateFee());
    }
}
