package org.upc.incidentservice.incident.domain.model.events;

import java.util.UUID;

public record IncidentAssignedEvent(UUID incidentId, Long responsibleUserId) {
}
