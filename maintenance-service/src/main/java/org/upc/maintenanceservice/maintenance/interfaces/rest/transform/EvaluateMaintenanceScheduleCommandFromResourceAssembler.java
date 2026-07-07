package org.upc.maintenanceservice.maintenance.interfaces.rest.transform;

import org.upc.maintenanceservice.maintenance.domain.model.commands.EvaluateMaintenanceScheduleCommand;

public class EvaluateMaintenanceScheduleCommandFromResourceAssembler {
    public static EvaluateMaintenanceScheduleCommand toCommandFromResource(Long scheduleId) {
        return new EvaluateMaintenanceScheduleCommand(scheduleId);
    }
}
