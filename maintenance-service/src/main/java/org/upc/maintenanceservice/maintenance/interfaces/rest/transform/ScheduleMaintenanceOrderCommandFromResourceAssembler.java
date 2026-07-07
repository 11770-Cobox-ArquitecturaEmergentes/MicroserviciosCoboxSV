package org.upc.maintenanceservice.maintenance.interfaces.rest.transform;

import org.upc.maintenanceservice.maintenance.domain.model.commands.ScheduleMaintenanceOrderCommand;
import org.upc.maintenanceservice.maintenance.interfaces.rest.resources.ScheduleMaintenanceOrderResource;

public class ScheduleMaintenanceOrderCommandFromResourceAssembler {
    public static ScheduleMaintenanceOrderCommand toCommandFromResource(Long orderId, ScheduleMaintenanceOrderResource resource) {
        return new ScheduleMaintenanceOrderCommand(orderId, resource.scheduledTimelapseDays());
    }
}
