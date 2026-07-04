package org.upc.maintenanceservice.maintenance.interfaces.rest.transform;

import org.upc.maintenanceservice.maintenance.domain.model.commands.UpdateMaintenanceRulesCommand;
import org.upc.maintenanceservice.maintenance.interfaces.rest.resources.UpdateMaintenanceRulesResource;

public class UpdateMaintenanceRulesCommandFromResourceAssembler {
    public static UpdateMaintenanceRulesCommand toCommandFromResource(Long scheduleId, UpdateMaintenanceRulesResource resource) {
        return new UpdateMaintenanceRulesCommand(
                scheduleId,
                MaintenanceRuleFromResourceAssembler.toEntitiesFromResources(resource.rules())
        );
    }
}
