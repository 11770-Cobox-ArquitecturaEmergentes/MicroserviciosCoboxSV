package org.upc.edgeservice.edge.interfaces.rest.resources;

import org.upc.edgeservice.edge.domain.model.valueobjects.EvidenceStatus;

import java.time.Instant;
import java.util.UUID;

public record EdgeEvidenceResource(
        UUID clientEvidenceId,
        Long orderId,
        Long routeId,
        String type,
        String objectKey,
        String sha256,
        String mimeType,
        Long sizeBytes,
        Instant capturedAt,
        EvidenceStatus status
) {
}
