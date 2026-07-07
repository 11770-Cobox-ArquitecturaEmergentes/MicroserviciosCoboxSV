package org.upc.incidentservice.incident.interfaces.rest.resources;

import java.time.LocalDateTime;
import java.util.UUID;

public record IncidentResource(
        Long id,
        UUID incidentId,
        String type,
        String description,
        LocalDateTime reportedAt,
        String severity,
        String status,
        Long responsibleUserId
) {
}
