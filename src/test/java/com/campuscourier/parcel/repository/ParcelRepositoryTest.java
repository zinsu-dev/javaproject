package com.campuscourier.parcel.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.campuscourier.parcel.model.BulkFreight;
import com.campuscourier.parcel.model.DeliveryStatus;
import com.campuscourier.parcel.model.DocumentParcel;
import com.campuscourier.parcel.model.FragileParcel;
import com.campuscourier.parcel.model.Parcel;
import com.campuscourier.rider.model.DispatchRider;
import com.campuscourier.rider.repository.DispatchRiderRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Persistence tests for the SINGLE_TABLE parcel hierarchy.
 *
 * HOW THEY RUN: @DataJpaTest starts a JPA slice (entities, repositories,
 * in-memory H2). Flyway migrates V1__create_initial_schema.sql FIRST, then
 * Hibernate VALIDATES the entities against the migrated schema — so these
 * tests prove the Java mappings and the SQL migration agree.
 * H2 runs in PostgreSQL compatibility mode, but that is still not a real
 * PostgreSQL: treat a live-Postgres run as the final check (see README).
 */
@DataJpaTest
class ParcelRepositoryTest {

    @Autowired
    private ParcelRepository parcelRepository;

    @Autowired
    private DispatchRiderRepository riderRepository;

    @Autowired
    private EntityManager entityManager;

    private static DocumentParcel document(String trackingId) {
        DocumentParcel parcel = new DocumentParcel(
                "Ama", "Kofi", "Hall A", "Library", new BigDecimal("1.500"));
        parcel.setTrackingId(trackingId);
        return parcel;
    }

    /** Reads the raw discriminator column, bypassing Hibernate mapping. */
    private String discriminatorOf(String trackingId) {
        return (String) entityManager.createNativeQuery(
                        "SELECT parcel_type FROM parcels WHERE tracking_id = :trackingId")
                .setParameter("trackingId", trackingId)
                .getSingleResult();
    }

    @Test
    void documentParcelPersistsAndLoadsBack() {
        Parcel saved = parcelRepository.saveAndFlush(document("CC-DOC0001"));

        entityManager.clear(); // force a real database read below

        Parcel loaded = parcelRepository.findByTrackingId("CC-DOC0001").orElseThrow();

        assertEquals(saved.getId(), loaded.getId());
        assertEquals(DocumentParcel.class, loaded.getClass());
        assertTrue(new BigDecimal("500").compareTo(loaded.calculateHandlingFee()) == 0);
    }

    @Test
    void fragileParcelPersistsWithCorrectFee() {
        FragileParcel parcel = new FragileParcel(
                "Ama", "Kofi", "Hall A", "Library", new BigDecimal("0.500"));
        parcel.setTrackingId("CC-FRG0001");
        parcelRepository.saveAndFlush(parcel);

        entityManager.clear();

        Parcel loaded = parcelRepository.findByTrackingId("CC-FRG0001").orElseThrow();

        assertEquals(FragileParcel.class, loaded.getClass());
        assertTrue(new BigDecimal("1500").compareTo(loaded.calculateHandlingFee()) == 0);
    }

    @Test
    void bulkFreightPersistsWithCorrectFee() {
        BulkFreight parcel = new BulkFreight(
                "Dept", "Store", "Gate", "Faculty", new BigDecimal("10.000"));
        parcel.setTrackingId("CC-BLK0001");
        parcelRepository.saveAndFlush(parcel);

        entityManager.clear();

        Parcel loaded = parcelRepository.findByTrackingId("CC-BLK0001").orElseThrow();

        assertEquals(BulkFreight.class, loaded.getClass());
        assertTrue(new BigDecimal("3000").compareTo(loaded.calculateHandlingFee()) == 0);
    }

    @Test
    void allThreeTypesShareOneTableWithCorrectDiscriminators() {
        parcelRepository.saveAndFlush(document("CC-DOC0002"));
        FragileParcel fragile = new FragileParcel("A", "B", "X", "Y", new BigDecimal("1"));
        fragile.setTrackingId("CC-FRG0002");
        parcelRepository.saveAndFlush(fragile);
        BulkFreight bulk = new BulkFreight("A", "B", "X", "Y", new BigDecimal("1"));
        bulk.setTrackingId("CC-BLK0002");
        parcelRepository.saveAndFlush(bulk);

        List<Parcel> all = parcelRepository.findAll();

        assertEquals(3, all.size());
        assertEquals("DOCUMENT", discriminatorOf("CC-DOC0002"));
        assertEquals("FRAGILE", discriminatorOf("CC-FRG0002"));
        assertEquals("BULK", discriminatorOf("CC-BLK0002"));
    }

    @Test
    void statusEnumPersistsAsString() {
        DocumentParcel parcel = document("CC-DOC0003");
        parcel.setStatus(DeliveryStatus.IN_TRANSIT);
        parcelRepository.saveAndFlush(parcel);

        entityManager.clear();

        Parcel loaded = parcelRepository.findByTrackingId("CC-DOC0003").orElseThrow();
        assertEquals(DeliveryStatus.IN_TRANSIT, loaded.getStatus());

        String rawStatus = (String) entityManager.createNativeQuery(
                        "SELECT status FROM parcels WHERE tracking_id = 'CC-DOC0003'")
                .getSingleResult();
        assertEquals("IN_TRANSIT", rawStatus);
    }

    @Test
    void duplicateTrackingIdIsRejected() {
        parcelRepository.saveAndFlush(document("CC-DUP0001"));

        assertThrows(DataIntegrityViolationException.class, () ->
                parcelRepository.saveAndFlush(document("CC-DUP0001")));
    }

    @Test
    void existsByTrackingIdWorks() {
        assertFalse(parcelRepository.existsByTrackingId("CC-NOPE001"));

        parcelRepository.saveAndFlush(document("CC-YES0001"));

        assertTrue(parcelRepository.existsByTrackingId("CC-YES0001"));
    }

    @Test
    void parcelCanExistWithoutARider() {
        parcelRepository.saveAndFlush(document("CC-NORIDER1"));

        entityManager.clear();

        Parcel loaded = parcelRepository.findByTrackingId("CC-NORIDER1").orElseThrow();
        assertNull(loaded.getRider());
    }

    @Test
    void parcelCanBeAssignedToARider() {
        DispatchRider rider = riderRepository.saveAndFlush(
                new DispatchRider("Yaw Mensah", "0244123456"));
        DocumentParcel parcel = document("CC-WITHRIDE");
        parcel.setRider(rider);
        parcelRepository.saveAndFlush(parcel);

        entityManager.clear(); // detach everything: the next read hits the DB

        Parcel loaded = parcelRepository.findByTrackingId("CC-WITHRIDE").orElseThrow();

        assertNotNull(loaded.getRider()); // LAZY proxy resolves inside the test transaction
        assertEquals("Yaw Mensah", loaded.getRider().getFullName());
        assertEquals(rider.getId(), loaded.getRider().getId());
    }
}
