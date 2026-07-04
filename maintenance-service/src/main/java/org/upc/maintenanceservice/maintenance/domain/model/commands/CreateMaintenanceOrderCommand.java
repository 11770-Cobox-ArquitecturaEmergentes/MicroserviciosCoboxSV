package org.upc.maintenanceservice.maintenance.domain.model.commands;

import org.upc.maintenanceservice.maintenance.domain.model.valueobjects.MaintenanceTypes;
import org.upc.maintenanceservice.maintenance.domain.model.valueobjects.Priorities;
import org.upc.maintenanceservice.maintenance.domain.model.valueobjects.Reason;

public record CreateMaintenanceOrderCommand(
        Long vehicleId,
        MaintenanceTypes maintenanceType,
        Priorities priority,
        Reason reason,
        Long openingOdometer,
        Integer scheduledTimelapseDays,
        Long technicianId
) {
}
