package org.upc.maintenanceservice.maintenance.interfaces.rest.transform;

import org.upc.maintenanceservice.maintenance.domain.model.commands.ActivateMaintenanceScheduleCommand;

public class ActivateMaintenanceScheduleCommandFromResourceAssembler {
    public static ActivateMaintenanceScheduleCommand toCommandFromResource(Long scheduleId) {
        return new ActivateMaintenanceScheduleCommand(scheduleId);
    }
}
