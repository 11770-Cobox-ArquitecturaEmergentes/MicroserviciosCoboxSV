package org.upc.edgeservice.edge.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.upc.edgeservice.edge.application.internal.commandservices.SyncBatchCommandService;
import org.upc.edgeservice.edge.domain.model.valueobjects.SyncBatchStatus;
import org.upc.edgeservice.edge.domain.model.valueobjects.SyncItemStatus;
import org.upc.edgeservice.edge.interfaces.rest.resources.SyncBatchRequest;
import org.upc.edgeservice.edge.interfaces.rest.resources.SyncBatchResponse;
import org.upc.edgeservice.edge.interfaces.rest.resources.SyncItemResultResource;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SyncBatchController.class)
@AutoConfigureMockMvc(addFilters = false)
class SyncBatchControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SyncBatchCommandService syncBatchCommandService;

    @Test
    void synchronizeReturnsAcceptedBatch() throws Exception {
        var clientBatchId = UUID.randomUUID();
        when(syncBatchCommandService.handle(any())).thenReturn(new SyncBatchResponse(
                1L,
                clientBatchId,
                SyncBatchStatus.ACCEPTED,
                List.of(new SyncItemResultResource(UUID.randomUUID(), 10L, SyncItemStatus.RECORDED, "Evidence recorded"))
        ));

        mockMvc.perform(post("/api/v1/edge/sync-batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(minimalRequest(clientBatchId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.batchId").value(1))
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.results[0].status").value("RECORDED"));
    }

    @Test
    void synchronizeReturnsDuplicateBatch() throws Exception {
        var clientBatchId = UUID.randomUUID();
        when(syncBatchCommandService.handle(any())).thenReturn(new SyncBatchResponse(
                1L,
                clientBatchId,
                SyncBatchStatus.DUPLICATE,
                List.of(new SyncItemResultResource(UUID.randomUUID(), 10L, SyncItemStatus.DUPLICATE, "Evidence already recorded"))
        ));

        mockMvc.perform(post("/api/v1/edge/sync-batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(minimalRequest(clientBatchId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DUPLICATE"))
                .andExpect(jsonPath("$.results[0].status").value("DUPLICATE"));
    }

    @Test
    void synchronizeReturnsBadRequestForRejectedHeader() throws Exception {
        when(syncBatchCommandService.handle(any())).thenReturn(new SyncBatchResponse(
                null,
                null,
                SyncBatchStatus.REJECTED,
                List.of(new SyncItemResultResource(null, null, SyncItemStatus.REJECTED, "clientBatchId is required"))
        ));

        mockMvc.perform(post("/api/v1/edge/sync-batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.results[0].status").value("REJECTED"));
    }

    private SyncBatchRequest minimalRequest(UUID clientBatchId) {
        return new SyncBatchRequest(
                clientBatchId,
                7L,
                "driver-phone-01",
                Instant.parse("2026-07-04T10:10:00Z"),
                List.of(),
                List.of(),
                List.of()
        );
    }
}
