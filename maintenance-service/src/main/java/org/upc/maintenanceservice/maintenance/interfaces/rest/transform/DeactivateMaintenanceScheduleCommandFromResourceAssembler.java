package org.upc.maintenanceservice.maintenance.interfaces.rest.transform;

import org.upc.maintenanceservice.maintenance.domain.model.commands.DeactivateMaintenanceScheduleCommand;

public class DeactivateMaintenanceScheduleCommandFromResourceAssembler {
    public static DeactivateMaintenanceScheduleCommand toCommandFromResource(Long scheduleId) {
        return new DeactivateMaintenanceScheduleCommand(scheduleId);
    }
}
