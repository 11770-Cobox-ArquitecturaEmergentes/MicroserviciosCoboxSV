package org.upc.aivalidationservice.validation.interfaces.messaging;

import java.time.Instant;
import java.util.UUID;

public record EvidenceUploadConfirmedEvent(
        UUID uploadIntentId,
        UUID clientEvidenceId,
        Long driverId,
        Long orderId,
        Long routeId,
        String type,
        String objectKey,
        String sha256,
        String mimeType,
        Long sizeBytes,
        Instant confirmedAt
) {
}
