package org.upc.incidentservice.incident.domain.model.commands;

import org.upc.incidentservice.incident.domain.model.valueobjects.IncidentStatus;

import java.util.UUID;

public record UpdateIncidentStatusCommand(UUID incidentId, IncidentStatus status) {
}
