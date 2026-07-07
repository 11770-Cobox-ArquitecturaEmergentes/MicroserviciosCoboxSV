package org.upc.mobilebffservice.mobile.infrastructure.storage;

import org.springframework.stereotype.Service;
import org.upc.mobilebffservice.mobile.domain.model.aggregates.UploadIntent;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class S3EvidenceStorageService implements EvidenceStorageService {

    public static final String CLIENT_EVIDENCE_ID_METADATA = "client-evidence-id";
    public static final String SHA256_METADATA = "sha256";

    private final StorageProperties properties;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    public S3EvidenceStorageService(StorageProperties properties, S3Client s3Client, S3Presigner s3Presigner) {
        this.properties = properties;
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    @Override
    public PresignedUpload createPresignedUpload(UploadIntent intent) {
        var metadata = Map.of(
                CLIENT_EVIDENCE_ID_METADATA, intent.getClientEvidenceId().toString(),
                SHA256_METADATA, intent.getSha256()
        );
        var putObjectRequest = PutObjectRequest.builder()
                .bucket(properties.bucket())
                .key(intent.getObjectKey())
                .contentType(intent.getMimeType())
                .contentLength(intent.getSizeBytes())
                .metadata(metadata)
                .build();
        var presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(properties.expirationMinutes()))
                .putObjectRequest(putObjectRequest)
                .build();
        var presignedRequest = s3Presigner.presignPutObject(presignRequest);
        var requiredHeaders = new LinkedHashMap<String, String>();
        requiredHeaders.put("Content-Type", intent.getMimeType());
        requiredHeaders.put("x-amz-meta-client-evidence-id", intent.getClientEvidenceId().toString());
        requiredHeaders.put("x-amz-meta-sha256", intent.getSha256());
        return new PresignedUpload(
                presignedRequest.url(),
                presignedRequest.httpRequest().method().name(),
                requiredHeaders,
                intent.getExpiresAt()
        );
    }

    @Override
    public UploadedObjectMetadata inspectObject(String objectKey) {
        var response = s3Client.headObject(HeadObjectRequest.builder()
                .bucket(properties.bucket())
                .key(objectKey)
                .build());
        return new UploadedObjectMetadata(
                response.contentLength(),
                response.contentType(),
                response.metadata()
        );
    }
}
