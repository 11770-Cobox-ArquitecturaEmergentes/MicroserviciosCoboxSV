package org.upc.desktopbffservice.desktop.infrastructure.clients.incident;

import java.time.LocalDateTime;
import java.util.UUID;

public record IncidentClientResource(
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
