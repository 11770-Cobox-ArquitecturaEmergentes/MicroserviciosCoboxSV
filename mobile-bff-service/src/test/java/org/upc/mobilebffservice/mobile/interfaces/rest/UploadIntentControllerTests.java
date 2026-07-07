package org.upc.mobilebffservice.mobile.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.upc.mobilebffservice.mobile.application.internal.commandservices.UploadIntentCommandService;
import org.upc.mobilebffservice.mobile.domain.model.valueobjects.UploadIntentStatus;
import org.upc.mobilebffservice.mobile.interfaces.rest.resources.CreateUploadIntentResource;
import org.upc.mobilebffservice.mobile.interfaces.rest.resources.UploadConfirmationResource;
import org.upc.mobilebffservice.mobile.interfaces.rest.resources.UploadIntentResource;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UploadIntentController.class)
@AutoConfigureMockMvc(addFilters = false)
class UploadIntentControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UploadIntentCommandService uploadIntentCommandService;

    @Test
    void createReturnsPresignedUploadIntent() throws Exception {
        var clientEvidenceId = UUID.randomUUID();
        var uploadIntentId = UUID.randomUUID();
        when(uploadIntentCommandService.create(any(), eq(7L))).thenReturn(new UploadIntentResource(
                uploadIntentId,
                clientEvidenceId,
                "drivers/7/routes/20/orders/100/evidences/" + clientEvidenceId,
                "https://s3.example.com/object",
                "PUT",
                Map.of("Content-Type", "image/jpeg"),
                Instant.parse("2026-07-04T10:15:00Z"),
                UploadIntentStatus.CREATED,
                "INCIDENT",
                "10"
        ));

        mockMvc.perform(post("/api/v1/mobile/evidence/upload-intents")
                        .with(jwt().jwt(token -> token.claim("driverId", 7L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest(clientEvidenceId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uploadIntentId").value(uploadIntentId.toString()))
                .andExpect(jsonPath("$.clientEvidenceId").value(clientEvidenceId.toString()))
                .andExpect(jsonPath("$.httpMethod").value("PUT"))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void confirmReturnsConfirmedStatus() throws Exception {
        var clientEvidenceId = UUID.randomUUID();
        var uploadIntentId = UUID.randomUUID();
        when(uploadIntentCommandService.confirm(eq(uploadIntentId), any())).thenReturn(new UploadConfirmationResource(
                uploadIntentId,
                clientEvidenceId,
                "drivers/7/routes/20/orders/100/evidences/" + clientEvidenceId,
                UploadIntentStatus.CONFIRMED,
                Instant.parse("2026-07-04T10:16:00Z")
        ));

        mockMvc.perform(post("/api/v1/mobile/evidence/upload-intents/{uploadIntentId}/confirm", uploadIntentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clientEvidenceId\":\"" + clientEvidenceId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.clientEvidenceId").value(clientEvidenceId.toString()));
    }

    @Test
    void multipartUploadIsNotSupported() throws Exception {
        mockMvc.perform(multipart("/api/v1/mobile/evidence/upload-intents")
                        .file("file", "binary".getBytes()))
                .andExpect(status().isUnsupportedMediaType());
    }

    private CreateUploadIntentResource validCreateRequest(UUID clientEvidenceId) {
        return new CreateUploadIntentResource(
                clientEvidenceId,
                null,
                100L,
                20L,
                "DELIVERY_PHOTO",
                "image/jpeg",
                2048L,
                "b6d81b360a5672d80c27430f39153e2c6f32f2255f6a071d9f8efb9bd2c7d1c2",
                null,
                null
        );
    }
}
