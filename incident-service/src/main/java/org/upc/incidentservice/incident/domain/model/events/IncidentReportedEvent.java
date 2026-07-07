package org.upc.incidentservice.incident.domain.model.events;

import org.upc.incidentservice.incident.domain.model.valueobjects.IncidentSeverity;
import org.upc.incidentservice.incident.domain.model.valueobjects.IncidentStatus;

import java.util.UUID;

public record IncidentReportedEvent(UUID incidentId, String type, IncidentSeverity severity, IncidentStatus status) {
}
