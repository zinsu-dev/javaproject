package com.campuscourier.parcel.repository;

import com.campuscourier.parcel.model.Parcel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * No implementation needed — Spring Data JPA builds it for us.
 * Method names become queries automatically:
 * findByTrackingNumber -> SELECT ... WHERE tracking_number = ?
 */
@Repository
public interface ParcelRepository extends JpaRepository<Parcel, Long> {

    Optional<Parcel> findByTrackingNumber(String trackingNumber);
}
