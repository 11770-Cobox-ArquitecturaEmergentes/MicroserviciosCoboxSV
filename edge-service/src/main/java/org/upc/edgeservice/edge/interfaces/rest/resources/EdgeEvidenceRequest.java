package org.upc.edgeservice.edge.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

public record EdgeEvidenceRequest(
        UUID clientEvidenceId,
        Long orderId,
        Long routeId,
        String type,
        String objectKey,
        String sha256,
        String mimeType,
        Long sizeBytes,
        Instant capturedAt
) {
}
