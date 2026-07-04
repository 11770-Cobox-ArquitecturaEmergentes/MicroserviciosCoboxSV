package org.upc.incidentservice.incident.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import org.upc.incidentservice.incident.domain.model.valueobjects.IncidentStatus;

public record UpdateIncidentStatusResource(@NotNull IncidentStatus status) {
}
