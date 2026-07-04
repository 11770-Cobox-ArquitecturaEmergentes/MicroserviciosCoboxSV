package org.upc.maintenanceservice.maintenance.interfaces.rest.transform;

import org.upc.maintenanceservice.maintenance.domain.model.commands.CreateMaintenanceOrderCommand;
import org.upc.maintenanceservice.maintenance.interfaces.rest.resources.CreateMaintenanceOrderResource;

public class CreateMaintenanceOrderCommandFromResourceAssembler {
    public static CreateMaintenanceOrderCommand toCommandFromResource(CreateMaintenanceOrderResource resource) {
        return new CreateMaintenanceOrderCommand(
                resource.vehicleId(),
                resource.maintenanceType(),
                resource.priority(),
                resource.reason(),
                resource.openingOdometer(),
                resource.scheduledTimelapseDays(),
                resource.technicianId()
        );
    }
}
