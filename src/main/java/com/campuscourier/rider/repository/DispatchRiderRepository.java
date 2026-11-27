package com.campuscourier.rider.repository;

import com.campuscourier.rider.model.DispatchRider;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Dispatch rider database access. findByAvailableTrue() is used when
 * assigning a parcel — only free riders are offered for dispatch.
 */
@Repository
public interface DispatchRiderRepository extends JpaRepository<DispatchRider, UUID> {

    List<DispatchRider> findByAvailableTrue();
}
