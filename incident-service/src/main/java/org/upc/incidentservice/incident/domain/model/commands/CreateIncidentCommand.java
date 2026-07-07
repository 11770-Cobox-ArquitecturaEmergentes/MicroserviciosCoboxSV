package org.upc.incidentservice.incident.domain.model.commands;

import org.upc.incidentservice.incident.domain.model.valueobjects.IncidentSeverity;

public record CreateIncidentCommand(String type, String description, IncidentSeverity severity, Long responsibleUserId) {
}
