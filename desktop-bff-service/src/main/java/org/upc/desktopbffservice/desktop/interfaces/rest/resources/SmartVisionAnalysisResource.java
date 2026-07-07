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
        String sourceType,
        String sourceId,
        String status,
        String provider,
        Double confidenceScore,
        Double fraudScore,
        String validationSummary,
        String failureReason,
        String reviewStatus,
        String reviewNotes,
        Instant reviewedAt,
        String previewUrl,
        Instant createdAt,
        Instant completedAt
) {
}
