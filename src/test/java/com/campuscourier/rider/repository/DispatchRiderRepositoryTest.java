package com.campuscourier.rider.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.campuscourier.rider.model.DispatchRider;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

/**
 * Persistence tests for DispatchRider. Runs in the same @DataJpaTest
 * setup as ParcelRepositoryTest (Flyway migration + Hibernate validate).
 */
@DataJpaTest
class DispatchRiderRepositoryTest {

    @Autowired
    private DispatchRiderRepository riderRepository;

    @Test
    void riderPersistsWithGeneratedIdAndAvailableTrue() {
        DispatchRider saved = riderRepository.saveAndFlush(
                new DispatchRider("Yaw Mensah", "0244123456"));

        assertNotNull(saved.getId());

        DispatchRider loaded = riderRepository.findById(saved.getId()).orElseThrow();

        assertEquals("Yaw Mensah", loaded.getFullName());
        assertEquals("0244123456", loaded.getPhoneNumber());
        assertEquals(Boolean.TRUE, loaded.isAvailable());
    }

    @Test
    void findByAvailableTrueReturnsOnlyFreeRiders() {
        riderRepository.saveAndFlush(new DispatchRider("Free Rider", "0200000001"));
        DispatchRider busy = new DispatchRider("Busy Rider", "0200000002");
        busy.setAvailable(Boolean.FALSE);
        riderRepository.saveAndFlush(busy);

        List<DispatchRider> free = riderRepository.findByAvailableTrue();

        assertEquals(1, free.size());
        assertEquals("Free Rider", free.get(0).getFullName());
    }

    @Test
    void updatingExistingRiderDoesNotDuplicateIt() {
        // save() on a NEW rider (null id) INSERTs...
        DispatchRider rider = riderRepository.saveAndFlush(
                new DispatchRider("Yaw Mensah", "0244123456"));

        // ...while save() on the LOADED rider (non-null id) UPDATEs.
        DispatchRider loaded = riderRepository.findById(rider.getId()).orElseThrow();
        loaded.setAvailable(Boolean.FALSE);
        riderRepository.saveAndFlush(loaded);

        assertEquals(1, riderRepository.count());
        assertEquals(Boolean.FALSE,
                riderRepository.findById(rider.getId()).orElseThrow().isAvailable());
    }
}
