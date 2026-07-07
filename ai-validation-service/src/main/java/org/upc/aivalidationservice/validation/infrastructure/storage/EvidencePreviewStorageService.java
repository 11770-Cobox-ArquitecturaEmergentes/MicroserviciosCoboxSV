package org.upc.aivalidationservice.validation.infrastructure.storage;

import org.springframework.stereotype.Service;
import org.upc.aivalidationservice.validation.interfaces.rest.resources.EvidencePreviewUrlResource;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;
import java.time.Instant;

@Service
public class EvidencePreviewStorageService {

    private static final Duration PREVIEW_EXPIRATION = Duration.ofMinutes(10);

    private final StorageProperties storageProperties;
    private final S3Presigner s3Presigner;

    public EvidencePreviewStorageService(StorageProperties storageProperties, S3Presigner s3Presigner) {
        this.storageProperties = storageProperties;
        this.s3Presigner = s3Presigner;
    }

    public EvidencePreviewUrlResource createPreviewUrl(String objectKey) {
        var expiresAt = Instant.now().plus(PREVIEW_EXPIRATION);
        var getObjectRequest = GetObjectRequest.builder()
                .bucket(storageProperties.bucket())
                .key(objectKey)
                .build();
        var presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(PREVIEW_EXPIRATION)
                .getObjectRequest(getObjectRequest)
                .build();
        var presigned = s3Presigner.presignGetObject(presignRequest);
        return new EvidencePreviewUrlResource(presigned.url().toString(), expiresAt);
    }
}
