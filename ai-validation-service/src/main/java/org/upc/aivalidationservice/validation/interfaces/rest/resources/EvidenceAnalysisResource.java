package org.upc.aivalidationservice.validation.interfaces.rest.resources;

import org.upc.aivalidationservice.validation.domain.model.valueobjects.AnalysisStatus;

import java.time.Instant;
import java.util.UUID;

public record EvidenceAnalysisResource(
        UUID clientEvidenceId,
        String objectKey,
        Long driverId,
        Long orderId,
        Long routeId,
        String evidenceType,
        AnalysisStatus status,
        String provider,
        Double confidenceScore,
        Double fraudScore,
        String validationSummary,
        String failureReason,
        Instant createdAt,
        Instant completedAt
) {
}
