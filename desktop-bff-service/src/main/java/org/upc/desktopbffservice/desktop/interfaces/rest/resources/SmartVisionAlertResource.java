package org.upc.desktopbffservice.desktop.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

public record SmartVisionAlertResource(
        UUID alertId,
        UUID clientEvidenceId,
        String type,
        String severity,
        String status,
        String message,
        Instant createdAt,
        Instant acknowledgedAt,
        Instant resolvedAt,
        String resolutionNotes,
        UUID linkedIncidentId
) {
}
