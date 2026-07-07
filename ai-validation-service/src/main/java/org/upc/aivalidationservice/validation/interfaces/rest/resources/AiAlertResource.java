package org.upc.aivalidationservice.validation.interfaces.rest.resources;

import org.upc.aivalidationservice.validation.domain.model.valueobjects.AlertSeverity;
import org.upc.aivalidationservice.validation.domain.model.valueobjects.AlertStatus;

import java.time.Instant;
import java.util.UUID;

public record AiAlertResource(
        UUID alertId,
        UUID clientEvidenceId,
        String type,
        AlertSeverity severity,
        AlertStatus status,
        String message,
        Instant createdAt,
        Instant acknowledgedAt,
        Instant resolvedAt,
        String resolutionNotes,
        UUID linkedIncidentId
) {
}
