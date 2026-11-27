package com.campuscourier.parcel.repository;

import com.campuscourier.parcel.model.Parcel;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Parcel database access. No implementation needed — Spring Data JPA
 * builds it at startup. Method names become queries automatically:
 * findByTrackingId -> SELECT ... WHERE tracking_id = ?
 */
@Repository
public interface ParcelRepository extends JpaRepository<Parcel, UUID> {

    Optional<Parcel> findByTrackingId(String trackingId);

    boolean existsByTrackingId(String trackingId);
}
