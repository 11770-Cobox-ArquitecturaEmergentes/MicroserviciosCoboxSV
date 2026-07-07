package org.upc.maintenanceservice.maintenance.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

public record CancelMaintenanceOrderResource(@NotBlank String reason) {
}
