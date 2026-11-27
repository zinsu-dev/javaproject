package com.campuscourier.parcel.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.campuscourier.common.exception.ResourceNotFoundException;
import com.campuscourier.parcel.dto.ParcelResponse;
import com.campuscourier.parcel.model.DocumentParcel;
import com.campuscourier.parcel.model.ParcelType;
import com.campuscourier.parcel.service.ParcelService;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * HTTP tests for ParcelController WITHOUT a database or server.
 * @WebMvcTest starts only the web layer (controller + validation +
 * exception handler); ParcelService is a Mockito fake; requests are
 * performed by MockMvc in-process.
 */
@WebMvcTest(ParcelController.class)
class ParcelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ParcelService parcelService;

    private static DocumentParcel document() {
        return new DocumentParcel("Ama", "Kofi", "Hall A", "Library", new BigDecimal("2.000"));
    }

    private static String validCreateJson() {
        return """
                {
                  "parcelType": "DOCUMENT",
                  "senderName": "Ama",
                  "recipientName": "Kofi",
                  "pickupLocation": "Hall A",
                  "destination": "Library",
                  "weight": 2.0
                }
                """;
    }

    @Test
    void postValidParcelReturns201WithLocationAndBody() throws Exception {
        ParcelResponse response = ParcelResponse.from(document());
        when(parcelService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/parcels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateJson()))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.trackingId").value(response.getTrackingId()))
                .andExpect(jsonPath("$.parcelType").value("DOCUMENT"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.handlingFee").value(500))
                .andExpect(jsonPath("$.totalFee").value(500));

        verify(parcelService).create(any());
    }

    @Test
    void postBlankSenderReturns400WithErrorShape() throws Exception {
        String invalid = validCreateJson().replace("\"senderName\": \"Ama\"", "\"senderName\": \"\"");

        mockMvc.perform(post("/api/parcels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/parcels"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void postUnknownParcelTypeReturns400() throws Exception {
        String invalid = validCreateJson().replace("DOCUMENT", "EXPRESS");

        mockMvc.perform(post("/api/parcels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getAllParcelsReturns200WithList() throws Exception {
        when(parcelService.findAll())
                .thenReturn(List.of(ParcelResponse.from(document())));

        mockMvc.perform(get("/api/parcels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].parcelType").value("DOCUMENT"));
    }

    @Test
    void getByIdReturns200() throws Exception {
        ParcelResponse response = ParcelResponse.from(document());
        UUID id = UUID.randomUUID();
        when(parcelService.findById(id)).thenReturn(response);

        mockMvc.perform(get("/api/parcels/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trackingId").value(response.getTrackingId()));
    }

    @Test
    void getByUnknownIdReturns404WithErrorShape() throws Exception {
        UUID id = UUID.randomUUID();
        when(parcelService.findById(id))
                .thenThrow(new ResourceNotFoundException("Parcel not found with id: " + id));

        mockMvc.perform(get("/api/parcels/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path").value("/api/parcels/" + id));
    }

    @Test
    void trackByTrackingIdReturns200() throws Exception {
        ParcelResponse response = ParcelResponse.from(document());
        when(parcelService.findByTrackingId("CC-ABC123")).thenReturn(response);

        mockMvc.perform(get("/api/parcels/track/{trackingId}", "CC-ABC123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trackingId").value(response.getTrackingId()));
    }

    @Test
    void trackUnknownCodeReturns404() throws Exception {
        when(parcelService.findByTrackingId("CC-NOPE"))
                .thenThrow(new ResourceNotFoundException("Parcel not found with tracking ID: CC-NOPE"));

        mockMvc.perform(get("/api/parcels/track/{trackingId}", "CC-NOPE"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void putValidUpdateReturns200WithChangedFields() throws Exception {
        ParcelResponse response = ParcelResponse.from(document());
        UUID id = UUID.randomUUID();
        when(parcelService.update(eq(id), any())).thenReturn(response);

        String updateJson = """
                {
                  "senderName": "Yaw",
                  "recipientName": "Esi",
                  "pickupLocation": "Gate",
                  "destination": "Faculty",
                  "weight": 3.0
                }
                """;

        mockMvc.perform(put("/api/parcels/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trackingId").value(response.getTrackingId()));
    }

    @Test
    void putInvalidWeightReturns400() throws Exception {
        UUID id = UUID.randomUUID();

        String updateJson = """
                {
                  "senderName": "Yaw",
                  "recipientName": "Esi",
                  "pickupLocation": "Gate",
                  "destination": "Faculty",
                  "weight": -1
                }
                """;

        mockMvc.perform(put("/api/parcels/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void deleteExistingReturns204() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/parcels/{id}", id))
                .andExpect(status().isNoContent());

        verify(parcelService).delete(id);
    }

    @Test
    void deleteUnknownIdReturns404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("Parcel not found with id: " + id))
                .when(parcelService).delete(id);

        mockMvc.perform(delete("/api/parcels/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void malformedUuidReturns400() throws Exception {
        mockMvc.perform(get("/api/parcels/{id}", "not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
