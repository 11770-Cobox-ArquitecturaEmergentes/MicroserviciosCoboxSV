package org.upc.maintenanceservice.maintenance.interfaces.rest.resources;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record CreateMaintenanceScheduleResource(
        @NotNull @Positive Long vehicleId,
        @NotEmpty @Valid List<MaintenanceRuleResource> rules
) {
}
