package com.campuscourier.parcel.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.campuscourier.common.exception.DuplicateTrackingIdException;
import com.campuscourier.common.exception.ResourceNotFoundException;
import com.campuscourier.parcel.dto.CreateParcelRequest;
import com.campuscourier.parcel.dto.ParcelResponse;
import com.campuscourier.parcel.dto.UpdateParcelRequest;
import com.campuscourier.parcel.factory.ParcelFactory;
import com.campuscourier.parcel.model.BulkFreight;
import com.campuscourier.parcel.model.DeliveryStatus;
import com.campuscourier.parcel.model.DocumentParcel;
import com.campuscourier.parcel.model.FragileParcel;
import com.campuscourier.parcel.model.ParcelType;
import com.campuscourier.parcel.repository.ParcelRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Service tests WITHOUT Spring or a database: ParcelRepository and
 * ParcelFactory are Mockito fakes, so failures point at service logic.
 * Uses the REAL factory (not a mock) so the correct-subclass wiring
 * is genuinely exercised.
 */
@ExtendWith(MockitoExtension.class)
class ParcelServiceTest {

    @Mock
    private ParcelRepository parcelRepository;

    private ParcelService parcelService;

    // Real factory (not a mock): the correct-subclass wiring is genuinely
    // exercised, while the database stays faked.
    @BeforeEach
    void setUp() {
        parcelService = new ParcelService(parcelRepository, new ParcelFactory());
    }

    private static CreateParcelRequest createRequest(ParcelType type) {
        CreateParcelRequest request = new CreateParcelRequest();
        request.setParcelType(type);
        request.setSenderName("Ama");
        request.setRecipientName("Kofi");
        request.setPickupLocation("Hall A");
        request.setDestination("Library");
        request.setWeight(new BigDecimal("2.000"));
        return request;
    }

    private static UpdateParcelRequest updateRequest() {
        UpdateParcelRequest request = new UpdateParcelRequest();
        request.setSenderName("Yaw");
        request.setRecipientName("Esi");
        request.setPickupLocation("Gate");
        request.setDestination("Faculty");
        request.setWeight(new BigDecimal("3.000"));
        return request;
    }

    @Test
    void createDocumentParcelPersistsAndReturnsFees() {
        when(parcelRepository.existsByTrackingId(any())).thenReturn(false);
        when(parcelRepository.save(any(DocumentParcel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ParcelResponse response = parcelService.create(createRequest(ParcelType.DOCUMENT));

        assertEquals(ParcelType.DOCUMENT, response.getParcelType());
        assertEquals(DeliveryStatus.PENDING, response.getStatus());
        assertTrue(new BigDecimal("500").compareTo(response.getHandlingFee()) == 0);
        assertTrue(new BigDecimal("500").compareTo(response.getTotalFee()) == 0);
        assertTrue(response.getTrackingId().startsWith("CC-"));
        verify(parcelRepository).save(any(DocumentParcel.class));
    }

    @Test
    void createFragileParcelUsesFragileFee() {
        when(parcelRepository.existsByTrackingId(any())).thenReturn(false);
        when(parcelRepository.save(any(FragileParcel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ParcelResponse response = parcelService.create(createRequest(ParcelType.FRAGILE));

        assertEquals(ParcelType.FRAGILE, response.getParcelType());
        assertTrue(new BigDecimal("1500").compareTo(response.getHandlingFee()) == 0);
    }

    @Test
    void createBulkFreightUsesBulkFee() {
        when(parcelRepository.existsByTrackingId(any())).thenReturn(false);
        when(parcelRepository.save(any(BulkFreight.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ParcelResponse response = parcelService.create(createRequest(ParcelType.BULK));

        assertEquals(ParcelType.BULK, response.getParcelType());
        assertTrue(new BigDecimal("3000").compareTo(response.getHandlingFee()) == 0);
    }

    @Test
    void createRejectsDuplicateTrackingId() {
        when(parcelRepository.existsByTrackingId(any())).thenReturn(true);

        assertThrows(DuplicateTrackingIdException.class,
                () -> parcelService.create(createRequest(ParcelType.DOCUMENT)));
        verify(parcelRepository, never()).save(any());
    }

    @Test
    void findAllMapsEveryParcel() {
        DocumentParcel first = new DocumentParcel("A", "B", "X", "Y", new BigDecimal("1"));
        FragileParcel second = new FragileParcel("A", "B", "X", "Y", new BigDecimal("1"));
        when(parcelRepository.findAll()).thenReturn(List.of(first, second));

        List<ParcelResponse> all = parcelService.findAll();

        assertEquals(2, all.size());
        assertEquals(ParcelType.DOCUMENT, all.get(0).getParcelType());
        assertEquals(ParcelType.FRAGILE, all.get(1).getParcelType());
    }

    @Test
    void findByIdReturnsParcel() {
        UUID id = UUID.randomUUID();
        when(parcelRepository.findById(id))
                .thenReturn(Optional.of(
                        new DocumentParcel("A", "B", "X", "Y", new BigDecimal("1"))));

        ParcelResponse response = parcelService.findById(id);

        assertEquals(ParcelType.DOCUMENT, response.getParcelType());
    }

    @Test
    void findByIdThrows404WhenMissing() {
        UUID id = UUID.randomUUID();
        when(parcelRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> parcelService.findById(id));
    }

    @Test
    void findByTrackingIdReturnsParcel() {
        DocumentParcel parcel = new DocumentParcel("A", "B", "X", "Y", new BigDecimal("1"));
        when(parcelRepository.findByTrackingId("CC-ABC123"))
                .thenReturn(Optional.of(parcel));

        ParcelResponse response = parcelService.findByTrackingId("CC-ABC123");

        assertEquals(parcel.getTrackingId(), response.getTrackingId());
    }

    @Test
    void findByTrackingIdThrows404WhenMissing() {
        when(parcelRepository.findByTrackingId("CC-NOPE")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> parcelService.findByTrackingId("CC-NOPE"));
    }

    @Test
    void updateChangesEditableFields() {
        UUID id = UUID.randomUUID();
        DocumentParcel parcel =
                new DocumentParcel("Old", "Old", "Old", "Old", new BigDecimal("1"));
        when(parcelRepository.findById(id)).thenReturn(Optional.of(parcel));

        ParcelResponse response = parcelService.update(id, updateRequest());

        assertEquals("Yaw", response.getSenderName());
        assertEquals("Esi", response.getRecipientName());
        assertEquals("Gate", response.getPickupLocation());
        assertEquals("Faculty", response.getDestination());
        assertTrue(new BigDecimal("3.000").compareTo(response.getWeight()) == 0);
    }

    @Test
    void updateThrows404WhenMissing() {
        UUID id = UUID.randomUUID();
        when(parcelRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> parcelService.update(id, updateRequest()));
    }

    @Test
    void deleteRemovesExistingParcel() {
        UUID id = UUID.randomUUID();
        DocumentParcel parcel =
                new DocumentParcel("A", "B", "X", "Y", new BigDecimal("1"));
        when(parcelRepository.findById(id)).thenReturn(Optional.of(parcel));

        parcelService.delete(id);

        verify(parcelRepository).delete(parcel);
    }

    @Test
    void deleteThrows404WhenMissing() {
        UUID id = UUID.randomUUID();
        when(parcelRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> parcelService.delete(id));
        verify(parcelRepository, never()).delete(any());
    }
}
