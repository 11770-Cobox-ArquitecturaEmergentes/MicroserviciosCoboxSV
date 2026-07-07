package org.upc.maintenanceservice.maintenance.interfaces.rest.transform;

import org.upc.maintenanceservice.maintenance.domain.model.commands.CreateMaintenanceScheduleCommand;
import org.upc.maintenanceservice.maintenance.interfaces.rest.resources.CreateMaintenanceScheduleResource;

public class CreateMaintenanceScheduleCommandFromResourceAssembler {
    public static CreateMaintenanceScheduleCommand toCommandFromResource(CreateMaintenanceScheduleResource resource) {
        return new CreateMaintenanceScheduleCommand(
                resource.vehicleId(),
                MaintenanceRuleFromResourceAssembler.toEntitiesFromResources(resource.rules())
        );
    }
}
