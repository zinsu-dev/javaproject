package com.campuscourier.parcel.controller;

import com.campuscourier.parcel.dto.CreateParcelRequest;
import com.campuscourier.parcel.dto.ParcelResponse;
import com.campuscourier.parcel.dto.UpdateStatusRequest;
import com.campuscourier.parcel.service.ParcelService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for parcels. Base path is versioned (/api/v1) so a future
 * mobile app or v2 API won't break the React frontend.
 */
@RestController
@RequestMapping("/api/v1/parcels")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class ParcelController {

    private final ParcelService parcelService;

    public ParcelController(ParcelService parcelService) {
        this.parcelService = parcelService;
    }

    /** Register a new parcel. */
    @PostMapping
    public ResponseEntity<ParcelResponse> create(@Valid @RequestBody CreateParcelRequest request) {
        ParcelResponse created = parcelService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** List every parcel (tracking dashboard). */
    @GetMapping
    public ResponseEntity<List<ParcelResponse>> findAll() {
        return ResponseEntity.ok(parcelService.findAll());
    }

    /** Get one parcel by database id. */
    @GetMapping("/{id}")
    public ResponseEntity<ParcelResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(parcelService.findById(id));
    }

    /** Track by the CC-XXXXXXXX code given at registration. */
    @GetMapping("/track/{trackingNumber}")
    public ResponseEntity<ParcelResponse> track(@PathVariable String trackingNumber) {
        return ResponseEntity.ok(parcelService.findByTrackingNumber(trackingNumber));
    }

    /** Move a parcel along REGISTERED -> ASSIGNED -> IN_TRANSIT -> DELIVERED. */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ParcelResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request) {
        return ResponseEntity.ok(parcelService.updateStatus(id, request.getStatus()));
    }

    /** Show the handling fee for a parcel (polymorphic calculation). */
    @GetMapping("/{id}/fee")
    public ResponseEntity<Map<String, Object>> fee(@PathVariable Long id) {
        ParcelResponse parcel = parcelService.findById(id);
        double fee = parcelService.calculateFee(id);
        return ResponseEntity.ok(Map.of(
                "parcelId", parcel.getId(),
                "trackingNumber", parcel.getTrackingNumber(),
                "parcelType", parcel.getParcelType().name(),
                "fee", fee));
    }

    /** Cancel / remove a parcel record. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        parcelService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
