package org.upc.incidentservice.incident.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AssignResponsibleUserResource(@NotNull @Positive Long responsibleUserId) {
}
