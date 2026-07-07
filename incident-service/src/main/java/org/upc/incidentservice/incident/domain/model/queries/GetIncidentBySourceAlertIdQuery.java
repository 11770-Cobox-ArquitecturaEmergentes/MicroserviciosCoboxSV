package org.upc.incidentservice.incident.domain.model.queries;

import java.util.UUID;

public record GetIncidentBySourceAlertIdQuery(UUID sourceAlertId) {
}
