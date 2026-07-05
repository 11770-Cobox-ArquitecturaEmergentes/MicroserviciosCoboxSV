package org.upc.desktopbffservice.desktop.interfaces.rest.resources;

import java.time.LocalDateTime;
import java.util.UUID;

public record IncidentSummaryResource(
        Long id,
        UUID incidentId,
        String type,
        String severity,
        String status,
        LocalDateTime reportedAt,
        Long responsibleUserId
) {
}
