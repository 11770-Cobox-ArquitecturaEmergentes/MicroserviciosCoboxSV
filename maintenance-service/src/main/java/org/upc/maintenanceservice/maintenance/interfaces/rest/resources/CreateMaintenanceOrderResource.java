package org.upc.maintenanceservice.maintenance.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.upc.maintenanceservice.maintenance.domain.model.valueobjects.MaintenanceTypes;
import org.upc.maintenanceservice.maintenance.domain.model.valueobjects.Priorities;
import org.upc.maintenanceservice.maintenance.domain.model.valueobjects.Reason;

public record CreateMaintenanceOrderResource(
        @NotNull @Positive Long vehicleId,
        @NotNull MaintenanceTypes maintenanceType,
        @NotNull Priorities priority,
        @NotNull Reason reason,
        @NotNull @Positive Long openingOdometer,
        @NotNull @Positive Integer scheduledTimelapseDays,
        Long technicianId
) {
}
