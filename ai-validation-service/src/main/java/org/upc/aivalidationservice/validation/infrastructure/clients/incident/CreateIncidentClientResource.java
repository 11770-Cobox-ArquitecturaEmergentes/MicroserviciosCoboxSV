package org.upc.aivalidationservice.validation.infrastructure.clients.incident;

import java.util.UUID;

public record CreateIncidentClientResource(
        String type,
        String description,
        String severity,
        Long responsibleUserId,
        String sourceType,
        UUID sourceAlertId,
        UUID sourceClientEvidenceId
) {
}
