package com.campuscourier.parcel.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Verifies the Phase 1 domain model as PURE Java — no Spring, no database.
 * Each test creates objects directly with "new", so failures always point
 * at domain logic, never at infrastructure.
 */
class ParcelTest {

    private static final BigDecimal WEIGHT = new BigDecimal("2.5");

    /**
     * BigDecimal.equals() also compares SCALE ("500" vs "500.00" are NOT
     * equal), so money assertions must use compareTo() == 0 instead.
     * This helper hides that quirk behind a readable name.
     */
    private static void assertMoneyEquals(String expectedNaira, BigDecimal actual) {
        assertNotNull(actual, "fee must never be null");
        assertTrue(new BigDecimal(expectedNaira).compareTo(actual) == 0,
                () -> "expected ₦" + expectedNaira + " but got ₦" + actual);
    }

    @Test
    void documentParcelHandlingFeeIs500() {
        Parcel parcel = new DocumentParcel("Ama", "Kofi", "Hall A", "Library", WEIGHT);

        assertMoneyEquals("500", parcel.calculateHandlingFee());
    }

    @Test
    void fragileParcelHandlingFeeIs1500() {
        Parcel parcel = new FragileParcel("Ama", "Kofi", "Hall A", "Library", WEIGHT);

        assertMoneyEquals("1500", parcel.calculateHandlingFee());
    }

    @Test
    void bulkFreightHandlingFeeIs3000() {
        Parcel parcel = new BulkFreight("Dept", "Store", "Gate", "Faculty", WEIGHT);

        assertMoneyEquals("3000", parcel.calculateHandlingFee());
    }

    @Test
    void polymorphicFeeCalculationPicksSubclassFormula() {
        // All three referenced through the PARENT type — the compiler only
        // knows "Parcel", yet each call runs the subclass override.
        List<Parcel> parcels = List.of(
                new DocumentParcel("A", "B", "X", "Y", WEIGHT),
                new FragileParcel("A", "B", "X", "Y", WEIGHT),
                new BulkFreight("A", "B", "X", "Y", WEIGHT));

        List<BigDecimal> fees = parcels.stream()
                .map(Parcel::calculateHandlingFee)
                .toList();

        assertMoneyEquals("500", fees.get(0));
        assertMoneyEquals("1500", fees.get(1));
        assertMoneyEquals("3000", fees.get(2));
    }

    @Test
    void totalFeeCurrentlyEqualsHandlingFee() {
        Parcel parcel = new FragileParcel("A", "B", "X", "Y", WEIGHT);

        assertMoneyEquals("1500", parcel.calculateTotalFee());
    }

    @Test
    void zeroWeightIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                new DocumentParcel("A", "B", "X", "Y", BigDecimal.ZERO));
    }

    @Test
    void negativeWeightIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                new BulkFreight("A", "B", "X", "Y", new BigDecimal("-1.5")));
    }

    @Test
    void nullWeightIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                new FragileParcel("A", "B", "X", "Y", null));
    }

    @Test
    void changingWeightToInvalidValueIsRejected() {
        Parcel parcel = new DocumentParcel("A", "B", "X", "Y", WEIGHT);

        assertThrows(IllegalArgumentException.class,
                () -> parcel.setWeight(new BigDecimal("0")));
    }

    @Test
    void newParcelsStartWithPendingStatus() {
        assertEquals(DeliveryStatus.PENDING,
                new DocumentParcel("A", "B", "X", "Y", WEIGHT).getStatus());
        assertEquals(DeliveryStatus.PENDING,
                new FragileParcel("A", "B", "X", "Y", WEIGHT).getStatus());
        assertEquals(DeliveryStatus.PENDING,
                new BulkFreight("A", "B", "X", "Y", WEIGHT).getStatus());
    }

    @Test
    void newParcelsGetUniqueIdsAndTrackingIds() {
        Parcel first = new DocumentParcel("A", "B", "X", "Y", WEIGHT);
        Parcel second = new DocumentParcel("A", "B", "X", "Y", WEIGHT);

        assertNotNull(first.getId());
        assertNotNull(first.getTrackingId());
        assertTrue(first.getTrackingId().startsWith("CC-"),
                "tracking ids look like CC-XXXXXXXX");
        // Random generation must not hand out the same identity twice.
        assertTrue(!first.getId().equals(second.getId()));
        assertTrue(!first.getTrackingId().equals(second.getTrackingId()));
    }
}
