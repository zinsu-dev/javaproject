package com.campuscourier.parcel.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.campuscourier.common.exception.ResourceNotFoundException;
import com.campuscourier.parcel.dto.CreateParcelRequest;
import com.campuscourier.parcel.dto.ParcelResponse;
import com.campuscourier.parcel.model.DocumentParcel;
import com.campuscourier.parcel.model.ParcelStatus;
import com.campuscourier.parcel.model.ParcelType;
import com.campuscourier.parcel.repository.ParcelRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests the service WITHOUT a database: the repository is a Mockito
 * fake, so we only verify service logic (mapping, tracking numbers...).
 */
@ExtendWith(MockitoExtension.class)
class ParcelServiceTest {

    @Mock
    private ParcelRepository parcelRepository;

    @InjectMocks
    private ParcelService parcelService;

    @Test
    void createDocumentParcelSavesAndReturnsResponse() {
        CreateParcelRequest request = new CreateParcelRequest();
        request.setParcelType(ParcelType.DOCUMENT);
        request.setSenderName("Ama");
        request.setRecipientName("Kofi");
        request.setPickupLocation("Hall A");
        request.setDropoffLocation("Library");
        request.setWeightKg(1.0);
        request.setDistanceKm(2.0);

        // Fake the DB save: return the same parcel back.
        when(parcelRepository.save(any(DocumentParcel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ParcelResponse response = parcelService.create(request);

        assertEquals(ParcelType.DOCUMENT, response.getParcelType());
        assertEquals(ParcelStatus.REGISTERED, response.getStatus());
        assertEquals(640.0, response.getFee(), 0.001);
        verify(parcelRepository).save(any(DocumentParcel.class));
    }

    @Test
    void findByIdThrows404WhenMissing() {
        when(parcelRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> parcelService.findById(99L));
    }
}
