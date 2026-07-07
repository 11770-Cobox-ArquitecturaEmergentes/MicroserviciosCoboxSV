package org.upc.incidentservice.incident.domain.model.queries;

import org.upc.incidentservice.incident.domain.model.valueobjects.IncidentStatus;

public record GetIncidentsByStatusQuery(IncidentStatus status) {
}
