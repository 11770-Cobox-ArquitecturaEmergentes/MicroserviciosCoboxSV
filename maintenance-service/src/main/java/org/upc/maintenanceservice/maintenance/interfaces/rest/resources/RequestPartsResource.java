package org.upc.maintenanceservice.maintenance.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RequestPartsResource(
        @NotBlank String partName,
        @NotNull @Positive Integer quantity
) {
}
