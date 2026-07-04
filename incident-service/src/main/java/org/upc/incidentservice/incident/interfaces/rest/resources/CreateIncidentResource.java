package org.upc.incidentservice.incident.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.upc.incidentservice.incident.domain.model.valueobjects.IncidentSeverity;

public record CreateIncidentResource(
        @NotBlank String type,
        @NotBlank @Size(max = 2000) String description,
        @NotNull IncidentSeverity severity,
        @Positive Long responsibleUserId
) {
}
