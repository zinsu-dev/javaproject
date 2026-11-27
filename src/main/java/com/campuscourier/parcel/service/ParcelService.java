package com.campuscourier.parcel.service;

import com.campuscourier.common.exception.ResourceNotFoundException;
import com.campuscourier.parcel.dto.CreateParcelRequest;
import com.campuscourier.parcel.dto.ParcelResponse;
import com.campuscourier.parcel.model.BulkFreight;
import com.campuscourier.parcel.model.DocumentParcel;
import com.campuscourier.parcel.model.FragileParcel;
import com.campuscourier.parcel.model.Parcel;
import com.campuscourier.parcel.model.ParcelStatus;
import com.campuscourier.parcel.repository.ParcelRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * All parcel business logic lives here — the controller only
 * handles HTTP, the repository only handles SQL.
 */
@Service
public class ParcelService {

    private final ParcelRepository parcelRepository;

    // Constructor injection (preferred over @Autowired fields:
    // easier to test with Mockito, impossible to forget a dependency).
    public ParcelService(ParcelRepository parcelRepository) {
        this.parcelRepository = parcelRepository;
    }

    @Transactional
    public ParcelResponse create(CreateParcelRequest request) {
        // Factory step: pick the right subclass from the request type.
        Parcel parcel = switch (request.getParcelType()) {
            case DOCUMENT -> new DocumentParcel(
                    request.getSenderName(), request.getRecipientName(),
                    request.getPickupLocation(), request.getDropoffLocation(),
                    request.getWeightKg(), request.getDistanceKm());
            case FRAGILE -> new FragileParcel(
                    request.getSenderName(), request.getRecipientName(),
                    request.getPickupLocation(), request.getDropoffLocation(),
                    request.getWeightKg(), request.getDistanceKm());
            case BULK -> new BulkFreight(
                    request.getSenderName(), request.getRecipientName(),
                    request.getPickupLocation(), request.getDropoffLocation(),
                    request.getWeightKg(), request.getDistanceKm());
        };

        parcel.setTrackingNumber(generateTrackingNumber());
        parcel.setStatus(ParcelStatus.REGISTERED);

        Parcel saved = parcelRepository.save(parcel);
        return ParcelResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ParcelResponse> findAll() {
        return parcelRepository.findAll()
                .stream()
                .map(ParcelResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ParcelResponse findById(Long id) {
        Parcel parcel = parcelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parcel not found with id: " + id));
        return ParcelResponse.from(parcel);
    }

    @Transactional(readOnly = true)
    public ParcelResponse findByTrackingNumber(String trackingNumber) {
        Parcel parcel = parcelRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Parcel not found with tracking number: " + trackingNumber));
        return ParcelResponse.from(parcel);
    }

    @Transactional
    public ParcelResponse updateStatus(Long id, ParcelStatus status) {
        Parcel parcel = parcelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parcel not found with id: " + id));
        parcel.setStatus(status);
        // No explicit save() needed — @Transactional + managed entity
        // means Hibernate flushes the change automatically.
        return ParcelResponse.from(parcel);
    }

    @Transactional(readOnly = true)
    public double calculateFee(Long id) {
        Parcel parcel = parcelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parcel not found with id: " + id));
        // Polymorphism: the JVM picks the subclass formula at runtime.
        return parcel.calculateFee();
    }

    @Transactional
    public void delete(Long id) {
        if (!parcelRepository.existsById(id)) {
            throw new ResourceNotFoundException("Parcel not found with id: " + id);
        }
        parcelRepository.deleteById(id);
    }

    private String generateTrackingNumber() {
        return "CC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
