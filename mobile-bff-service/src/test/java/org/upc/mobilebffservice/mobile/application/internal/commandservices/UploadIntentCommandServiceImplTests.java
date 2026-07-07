package org.upc.mobilebffservice.mobile.application.internal.commandservices;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import org.upc.mobilebffservice.mobile.domain.exceptions.UploadConfirmationException;
import org.upc.mobilebffservice.mobile.domain.model.valueobjects.UploadIntentStatus;
import org.upc.mobilebffservice.mobile.infrastructure.persistence.jpa.repositories.OutboxMessageRepository;
import org.upc.mobilebffservice.mobile.infrastructure.persistence.jpa.repositories.UploadIntentRepository;
import org.upc.mobilebffservice.mobile.infrastructure.storage.EvidenceStorageService;
import org.upc.mobilebffservice.mobile.infrastructure.storage.PresignedUpload;
import org.upc.mobilebffservice.mobile.infrastructure.storage.S3EvidenceStorageService;
import org.upc.mobilebffservice.mobile.infrastructure.storage.UploadedObjectMetadata;
import org.upc.mobilebffservice.mobile.interfaces.rest.resources.ConfirmUploadIntentResource;
import org.upc.mobilebffservice.mobile.interfaces.rest.resources.CreateUploadIntentResource;

import java.net.URL;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class UploadIntentCommandServiceImplTests {

    @Autowired
    private UploadIntentCommandService uploadIntentCommandService;

    @Autowired
    private UploadIntentRepository uploadIntentRepository;

    @Autowired
    private OutboxMessageRepository outboxMessageRepository;

    @MockBean
    private EvidenceStorageService evidenceStorageService;

    @Test
    void createIsIdempotentByClientEvidenceId() throws Exception {
        when(evidenceStorageService.createPresignedUpload(any())).thenReturn(presignedUpload());
        var clientEvidenceId = UUID.randomUUID();
        var request = validCreateRequest(clientEvidenceId);

        var first = uploadIntentCommandService.create(request, request.driverId());
        var second = uploadIntentCommandService.create(request, request.driverId());

        assertThat(second.uploadIntentId()).isEqualTo(first.uploadIntentId());
        assertThat(second.objectKey()).isEqualTo(first.objectKey());
        assertThat(uploadIntentRepository.count()).isEqualTo(1);
    }

    @Test
    void confirmValidatesS3MetadataAndCreatesOutbox() throws Exception {
        when(evidenceStorageService.createPresignedUpload(any())).thenReturn(presignedUpload());
        var clientEvidenceId = UUID.randomUUID();
        var request = validCreateRequest(clientEvidenceId);
        var intent = uploadIntentCommandService.create(request, request.driverId());
        when(evidenceStorageService.inspectObject(anyString())).thenReturn(validMetadata(clientEvidenceId));

        var confirmation = uploadIntentCommandService.confirm(
                intent.uploadIntentId(),
                new ConfirmUploadIntentResource(clientEvidenceId)
        );

        assertThat(confirmation.status()).isEqualTo(UploadIntentStatus.CONFIRMED);
        assertThat(confirmation.confirmedAt()).isNotNull();
        assertThat(outboxMessageRepository.count()).isEqualTo(1);
    }

    @Test
    void repeatedConfirmDoesNotDuplicateOutbox() throws Exception {
        when(evidenceStorageService.createPresignedUpload(any())).thenReturn(presignedUpload());
        var clientEvidenceId = UUID.randomUUID();
        var request = validCreateRequest(clientEvidenceId);
        var intent = uploadIntentCommandService.create(request, request.driverId());
        when(evidenceStorageService.inspectObject(anyString())).thenReturn(validMetadata(clientEvidenceId));

        uploadIntentCommandService.confirm(intent.uploadIntentId(), new ConfirmUploadIntentResource(clientEvidenceId));
        uploadIntentCommandService.confirm(intent.uploadIntentId(), new ConfirmUploadIntentResource(clientEvidenceId));

        assertThat(outboxMessageRepository.count()).isEqualTo(1);
    }

    @Test
    void confirmRejectsSizeMismatch() throws Exception {
        when(evidenceStorageService.createPresignedUpload(any())).thenReturn(presignedUpload());
        var clientEvidenceId = UUID.randomUUID();
        var request = validCreateRequest(clientEvidenceId);
        var intent = uploadIntentCommandService.create(request, request.driverId());
        when(evidenceStorageService.inspectObject(anyString())).thenReturn(new UploadedObjectMetadata(
                1L,
                "image/jpeg",
                Map.of(
                        S3EvidenceStorageService.CLIENT_EVIDENCE_ID_METADATA, clientEvidenceId.toString(),
                        S3EvidenceStorageService.SHA256_METADATA, request.sha256()
                )
        ));

        assertThatThrownBy(() -> uploadIntentCommandService.confirm(
                intent.uploadIntentId(),
                new ConfirmUploadIntentResource(clientEvidenceId)
        )).isInstanceOf(UploadConfirmationException.class);
    }

    private CreateUploadIntentResource validCreateRequest(UUID clientEvidenceId) {
        return new CreateUploadIntentResource(
                clientEvidenceId,
                7L,
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

    private UploadedObjectMetadata validMetadata(UUID clientEvidenceId) {
        return new UploadedObjectMetadata(
                2048L,
                "image/jpeg",
                Map.of(
                        S3EvidenceStorageService.CLIENT_EVIDENCE_ID_METADATA, clientEvidenceId.toString(),
                        S3EvidenceStorageService.SHA256_METADATA, "b6d81b360a5672d80c27430f39153e2c6f32f2255f6a071d9f8efb9bd2c7d1c2"
                )
        );
    }

    private PresignedUpload presignedUpload() throws Exception {
        return new PresignedUpload(
                new URL("https://s3.example.com/object"),
                "PUT",
                Map.of(
                        "Content-Type", "image/jpeg",
                        "x-amz-meta-client-evidence-id", UUID.randomUUID().toString(),
                        "x-amz-meta-sha256", "sha"
                ),
                Instant.now().plusSeconds(900)
        );
    }
}
