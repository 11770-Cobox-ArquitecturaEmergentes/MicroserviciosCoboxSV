package org.upc.mobilebffservice.mobile.application.internal.commandservices;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.upc.mobilebffservice.mobile.domain.exceptions.InvalidUploadIntentException;
import org.upc.mobilebffservice.mobile.domain.exceptions.UploadConfirmationException;
import org.upc.mobilebffservice.mobile.domain.exceptions.UploadIntentNotFoundException;
import org.upc.mobilebffservice.mobile.domain.model.aggregates.UploadIntent;
import org.upc.mobilebffservice.mobile.domain.model.entities.OutboxMessage;
import org.upc.mobilebffservice.mobile.domain.model.valueobjects.UploadIntentStatus;
import org.upc.mobilebffservice.mobile.infrastructure.persistence.jpa.repositories.OutboxMessageRepository;
import org.upc.mobilebffservice.mobile.infrastructure.persistence.jpa.repositories.UploadIntentRepository;
import org.upc.mobilebffservice.mobile.infrastructure.storage.EvidenceStorageService;
import org.upc.mobilebffservice.mobile.infrastructure.storage.PresignedUpload;
import org.upc.mobilebffservice.mobile.infrastructure.storage.S3EvidenceStorageService;
import org.upc.mobilebffservice.mobile.infrastructure.storage.StorageProperties;
import org.upc.mobilebffservice.mobile.interfaces.rest.resources.ConfirmUploadIntentResource;
import org.upc.mobilebffservice.mobile.interfaces.rest.resources.CreateUploadIntentResource;
import org.upc.mobilebffservice.mobile.interfaces.rest.resources.UploadConfirmationResource;
import org.upc.mobilebffservice.mobile.interfaces.rest.resources.UploadIntentResource;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class UploadIntentCommandServiceImpl implements UploadIntentCommandService {

    public static final String EVIDENCE_UPLOAD_CONFIRMED = "EvidenceUploadConfirmed";
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of("image/jpeg", "image/png", "application/pdf");

    private final UploadIntentRepository uploadIntentRepository;
    private final OutboxMessageRepository outboxMessageRepository;
    private final EvidenceStorageService evidenceStorageService;
    private final StorageProperties storageProperties;
    private final ObjectMapper objectMapper;

    public UploadIntentCommandServiceImpl(UploadIntentRepository uploadIntentRepository,
                                          OutboxMessageRepository outboxMessageRepository,
                                          EvidenceStorageService evidenceStorageService,
                                          StorageProperties storageProperties,
                                          ObjectMapper objectMapper) {
        this.uploadIntentRepository = uploadIntentRepository;
        this.outboxMessageRepository = outboxMessageRepository;
        this.evidenceStorageService = evidenceStorageService;
        this.storageProperties = storageProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public UploadIntentResource create(CreateUploadIntentResource resource, Long driverId) {
        validateCreate(resource, driverId);
        var existing = uploadIntentRepository.findByClientEvidenceId(resource.clientEvidenceId());
        if (existing.isPresent()) {
            var intent = existing.get();
            if (intent.getStatus() == UploadIntentStatus.CONFIRMED) {
                return toResource(intent, null);
            }
            if (intent.isExpired(Instant.now())) {
                intent.markExpired();
                intent.refreshExpiration(nextExpiration());
                uploadIntentRepository.save(intent);
            }
            return toResource(intent, evidenceStorageService.createPresignedUpload(intent));
        }

        var intent = uploadIntentRepository.save(new UploadIntent(
                resource.clientEvidenceId(),
                driverId,
                resource.orderId(),
                resource.routeId(),
                resource.type().trim(),
                clean(resource.sourceType()),
                clean(resource.sourceId()),
                objectKey(driverId, resource.routeId(), resource.orderId(), resource.clientEvidenceId()),
                resource.sha256().trim(),
                resource.mimeType().trim(),
                resource.sizeBytes(),
                nextExpiration()
        ));
        return toResource(intent, evidenceStorageService.createPresignedUpload(intent));
    }

    @Override
    @Transactional
    public UploadConfirmationResource confirm(UUID uploadIntentId, ConfirmUploadIntentResource resource) {
        if (uploadIntentId == null) {
            throw new InvalidUploadIntentException("uploadIntentId is required");
        }
        if (resource == null || resource.clientEvidenceId() == null) {
            throw new InvalidUploadIntentException("clientEvidenceId is required");
        }

        var intent = uploadIntentRepository.findByUploadIntentId(uploadIntentId)
                .orElseThrow(() -> new UploadIntentNotFoundException(uploadIntentId));
        if (!intent.getClientEvidenceId().equals(resource.clientEvidenceId())) {
            throw new UploadConfirmationException("clientEvidenceId does not match upload intent");
        }
        if (intent.getStatus() == UploadIntentStatus.CONFIRMED) {
            ensureOutbox(intent);
            return toConfirmationResource(intent);
        }

        try {
            var metadata = evidenceStorageService.inspectObject(intent.getObjectKey());
            validateObjectMetadata(intent, metadata.contentLength(), metadata.contentType(), metadata.metadata());
            intent.markConfirmed(Instant.now());
            uploadIntentRepository.save(intent);
            ensureOutbox(intent);
            return toConfirmationResource(intent);
        } catch (NoSuchKeyException ex) {
            intent.markFailed();
            uploadIntentRepository.save(intent);
            throw new UploadConfirmationException("S3 object does not exist for upload intent");
        } catch (S3Exception ex) {
            intent.markFailed();
            uploadIntentRepository.save(intent);
            throw new UploadConfirmationException("S3 object validation failed: " + ex.awsErrorDetails().errorMessage());
        }
    }

    private void validateCreate(CreateUploadIntentResource resource, Long driverId) {
        if (resource == null) throw new InvalidUploadIntentException("Request body is required");
        if (resource.clientEvidenceId() == null) throw new InvalidUploadIntentException("clientEvidenceId is required");
        if (driverId == null) throw new InvalidUploadIntentException("driverId is required");
        if (resource.orderId() == null) throw new InvalidUploadIntentException("orderId is required");
        if (resource.routeId() == null) throw new InvalidUploadIntentException("routeId is required");
        if (isBlank(resource.type())) throw new InvalidUploadIntentException("type is required");
        if (isBlank(resource.mimeType())) throw new InvalidUploadIntentException("mimeType is required");
        if (!ALLOWED_MIME_TYPES.contains(resource.mimeType().trim())) {
            throw new InvalidUploadIntentException("mimeType is not allowed");
        }
        if (isBlank(resource.sha256())) throw new InvalidUploadIntentException("sha256 is required");
        if (resource.sizeBytes() == null || resource.sizeBytes() <= 0) {
            throw new InvalidUploadIntentException("sizeBytes must be greater than 0");
        }
    }

    private void validateObjectMetadata(UploadIntent intent, long contentLength, String contentType, Map<String, String> metadata) {
        if (contentLength != intent.getSizeBytes()) {
            throw new UploadConfirmationException("S3 object size does not match upload intent");
        }
        if (contentType == null || !contentType.equals(intent.getMimeType())) {
            throw new UploadConfirmationException("S3 object content type does not match upload intent");
        }
        var clientEvidenceId = metadata.get(S3EvidenceStorageService.CLIENT_EVIDENCE_ID_METADATA);
        var sha256 = metadata.get(S3EvidenceStorageService.SHA256_METADATA);
        if (!intent.getClientEvidenceId().toString().equals(clientEvidenceId)) {
            throw new UploadConfirmationException("S3 object client evidence metadata does not match upload intent");
        }
        if (!intent.getSha256().equals(sha256)) {
            throw new UploadConfirmationException("S3 object sha256 metadata does not match upload intent");
        }
    }

    private void ensureOutbox(UploadIntent intent) {
        var aggregateId = intent.getClientEvidenceId().toString();
        if (outboxMessageRepository.existsByEventTypeAndAggregateId(EVIDENCE_UPLOAD_CONFIRMED, aggregateId)) {
            return;
        }
        outboxMessageRepository.save(new OutboxMessage(
                EVIDENCE_UPLOAD_CONFIRMED,
                aggregateId,
                payload(intent),
                Instant.now()
        ));
    }

    private String payload(UploadIntent intent) {
        try {
            var payload = new LinkedHashMap<String, Object>();
            payload.put("uploadIntentId", intent.getUploadIntentId().toString());
            payload.put("clientEvidenceId", intent.getClientEvidenceId().toString());
            payload.put("driverId", intent.getDriverId());
            payload.put("orderId", intent.getOrderId());
            payload.put("routeId", intent.getRouteId());
            payload.put("type", intent.getType());
            payload.put("sourceType", intent.getSourceType());
            payload.put("sourceId", intent.getSourceId());
            payload.put("objectKey", intent.getObjectKey());
            payload.put("sha256", intent.getSha256());
            payload.put("mimeType", intent.getMimeType());
            payload.put("sizeBytes", intent.getSizeBytes());
            payload.put("confirmedAt", intent.getConfirmedAt().toString());
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new UploadConfirmationException("Could not serialize EvidenceUploadConfirmed payload");
        }
    }

    private UploadIntentResource toResource(UploadIntent intent, PresignedUpload upload) {
        return new UploadIntentResource(
                intent.getUploadIntentId(),
                intent.getClientEvidenceId(),
                intent.getObjectKey(),
                upload == null ? null : upload.uploadUrl().toString(),
                upload == null ? "PUT" : upload.httpMethod(),
                upload == null ? Map.of() : upload.requiredHeaders(),
                intent.getExpiresAt(),
                intent.getStatus(),
                intent.getSourceType(),
                intent.getSourceId()
        );
    }

    private UploadConfirmationResource toConfirmationResource(UploadIntent intent) {
        return new UploadConfirmationResource(
                intent.getUploadIntentId(),
                intent.getClientEvidenceId(),
                intent.getObjectKey(),
                intent.getStatus(),
                intent.getConfirmedAt()
        );
    }

    private String objectKey(Long driverId, Long routeId, Long orderId, UUID clientEvidenceId) {
        return "drivers/%d/routes/%d/orders/%d/evidences/%s".formatted(driverId, routeId, orderId, clientEvidenceId);
    }

    private Instant nextExpiration() {
        return Instant.now().plus(Duration.ofMinutes(storageProperties.expirationMinutes()));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String clean(String value) {
        return isBlank(value) ? null : value.trim();
    }
}
