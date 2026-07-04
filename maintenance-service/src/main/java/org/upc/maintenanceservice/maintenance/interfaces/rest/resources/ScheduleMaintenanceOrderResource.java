package org.upc.maintenanceservice.maintenance.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ScheduleMaintenanceOrderResource(@NotNull @Positive Integer scheduledTimelapseDays) {
}
