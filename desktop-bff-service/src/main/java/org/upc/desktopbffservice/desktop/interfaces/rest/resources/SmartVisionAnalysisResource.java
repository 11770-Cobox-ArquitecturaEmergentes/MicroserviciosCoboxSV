package org.upc.desktopbffservice.desktop.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

public record SmartVisionAnalysisResource(
        UUID clientEvidenceId,
        String objectKey,
        Long driverId,
        Long orderId,
        Long routeId,
        String evidenceType,
        String status,
        String provider,
        Double confidenceScore,
        Double fraudScore,
        String validationSummary,
        String failureReason,
        Instant createdAt,
        Instant completedAt
) {
}
