package org.upc.incidentservice.incident.domain.model.events;

import org.upc.incidentservice.incident.domain.model.valueobjects.IncidentStatus;

import java.util.UUID;

public record IncidentStatusUpdatedEvent(UUID incidentId, IncidentStatus status) {
}
