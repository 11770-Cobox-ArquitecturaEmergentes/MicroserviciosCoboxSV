package org.upc.aivalidationservice.validation.infrastructure.clients.edge;

import java.time.Instant;
import java.util.UUID;

public record EdgeEvidenceClientResource(
        UUID clientEvidenceId,
        Long orderId,
        Long routeId,
        String type,
        String objectKey,
        String sha256,
        String mimeType,
        Long sizeBytes,
        Instant capturedAt,
        String status
) {
}
