package org.upc.incidentservice.incident.domain.model.commands;

import org.upc.incidentservice.incident.domain.model.valueobjects.IncidentSeverity;

import java.util.UUID;

public record CreateIncidentCommand(
        String type,
        String description,
        IncidentSeverity severity,
        Long responsibleUserId,
        String sourceType,
        UUID sourceAlertId,
        UUID sourceClientEvidenceId
) {
}
