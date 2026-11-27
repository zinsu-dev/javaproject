package com.campuscourier.parcel.controller;

import com.campuscourier.parcel.dto.CreateParcelRequest;
import com.campuscourier.parcel.dto.ParcelResponse;
import com.campuscourier.parcel.dto.UpdateParcelRequest;
import com.campuscourier.parcel.service.ParcelService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Parcel REST API.
 *
 * THIN controllers on purpose: each method validates input (@Valid),
 * delegates to ParcelService, and shapes the HTTP response. No fee
 * math, no entity construction, no database calls happen here.
 */
@RestController
@RequestMapping("/api/parcels")
public class ParcelController {

    private final ParcelService parcelService;

    public ParcelController(ParcelService parcelService) {
        this.parcelService = parcelService;
    }

    /** Register a parcel → 201 Created + Location header + body. */
    @PostMapping
    public ResponseEntity<ParcelResponse> create(@Valid @RequestBody CreateParcelRequest request) {
        ParcelResponse created = parcelService.create(request);
        return ResponseEntity
                .created(URI.create("/api/parcels/" + created.getId()))
                .body(created);
    }

    /** List every parcel (tracking dashboard). */
    @GetMapping
    public ResponseEntity<List<ParcelResponse>> findAll() {
        return ResponseEntity.ok(parcelService.findAll());
    }

    /** Get one parcel by its UUID. A malformed UUID returns 400. */
    @GetMapping("/{id}")
    public ResponseEntity<ParcelResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(parcelService.findById(id));
    }

    /** Track by the CC-XXXXXXXX code given at registration. */
    @GetMapping("/track/{trackingId}")
    public ResponseEntity<ParcelResponse> track(@PathVariable String trackingId) {
        return ResponseEntity.ok(parcelService.findByTrackingId(trackingId));
    }

    /** Replace the editable delivery details of a parcel. */
    @PutMapping("/{id}")
    public ResponseEntity<ParcelResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateParcelRequest request) {
        return ResponseEntity.ok(parcelService.update(id, request));
    }

    /** Delete a parcel → 204 No Content (empty body). */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        parcelService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
