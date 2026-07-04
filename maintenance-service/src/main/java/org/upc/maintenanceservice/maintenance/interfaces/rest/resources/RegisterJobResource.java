package org.upc.maintenanceservice.maintenance.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

public record RegisterJobResource(@NotBlank String description, Boolean completed) {
}
