package com.campuscourier.parcel.service;

import com.campuscourier.common.exception.DuplicateTrackingIdException;
import com.campuscourier.common.exception.ResourceNotFoundException;
import com.campuscourier.parcel.dto.CreateParcelRequest;
import com.campuscourier.parcel.dto.ParcelResponse;
import com.campuscourier.parcel.dto.UpdateParcelRequest;
import com.campuscourier.parcel.factory.ParcelFactory;
import com.campuscourier.parcel.model.Parcel;
import com.campuscourier.parcel.repository.ParcelRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Parcel business logic. The controller handles HTTP, the repository
 * handles SQL — everything in between lives here.
 *
 * TRANSACTIONS: @Transactional on write methods means "all database
 * changes in this method succeed or roll back together". Read methods
 * are marked readOnly so Hibernate skips dirty-checking overhead.
 */
@Service
public class ParcelService {

    private final ParcelRepository parcelRepository;
    private final ParcelFactory parcelFactory;

    // Constructor injection: dependencies are explicit, and tests can
    // pass Mockito fakes without starting Spring.
    public ParcelService(ParcelRepository parcelRepository, ParcelFactory parcelFactory) {
        this.parcelRepository = parcelRepository;
        this.parcelFactory = parcelFactory;
    }

    /**
     * Registers a parcel: builds the right subclass, persists it, and
     * returns the response. trackingId and PENDING status are assigned
     * inside the entity constructor — never accepted from the client.
     */
    @Transactional
    public ParcelResponse create(CreateParcelRequest request) {
        Parcel parcel = parcelFactory.create(
                request.getParcelType(),
                request.getSenderName(),
                request.getRecipientName(),
                request.getPickupLocation(),
                request.getDestination(),
                request.getWeight());

        // Tracking ids are random 8-hex strings; a collision is
        // practically impossible, but fail loudly (409) instead of
        // letting the database throw a raw constraint error.
        if (parcelRepository.existsByTrackingId(parcel.getTrackingId())) {
            throw new DuplicateTrackingIdException(
                    "Tracking ID already exists: " + parcel.getTrackingId());
        }

        return ParcelResponse.from(parcelRepository.save(parcel));
    }

    @Transactional(readOnly = true)
    public List<ParcelResponse> findAll() {
        return parcelRepository.findAll()
                .stream()
                .map(ParcelResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ParcelResponse findById(UUID id) {
        return ParcelResponse.from(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public ParcelResponse findByTrackingId(String trackingId) {
        Parcel parcel = parcelRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Parcel not found with tracking ID: " + trackingId));
        return ParcelResponse.from(parcel);
    }

    /**
     * Updates the sender-editable delivery details. Status, rider and
     * fees are intentionally not updatable here (later phases own them).
     * No explicit save() call: the entity is managed inside this
     * transaction, so Hibernate flushes the changes automatically.
     */
    @Transactional
    public ParcelResponse update(UUID id, UpdateParcelRequest request) {
        Parcel parcel = findEntityById(id);
        parcel.setSenderName(request.getSenderName());
        parcel.setRecipientName(request.getRecipientName());
        parcel.setPickupLocation(request.getPickupLocation());
        parcel.setDestination(request.getDestination());
        parcel.setWeight(request.getWeight());
        return ParcelResponse.from(parcel);
    }

    @Transactional
    public void delete(UUID id) {
        Parcel parcel = findEntityById(id);
        parcelRepository.delete(parcel);
    }

    private Parcel findEntityById(UUID id) {
        return parcelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Parcel not found with id: " + id));
    }
}
