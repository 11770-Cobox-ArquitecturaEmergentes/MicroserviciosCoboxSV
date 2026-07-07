package org.upc.maintenanceservice.maintenance.interfaces.rest.transform;

import org.upc.maintenanceservice.maintenance.domain.model.commands.CompleteMaintenanceOrderCommand;
import org.upc.maintenanceservice.maintenance.interfaces.rest.resources.CompleteMaintenanceOrderResource;

public class CompleteMaintenanceOrderCommandFromResourceAssembler {
    public static CompleteMaintenanceOrderCommand toCommandFromResource(Long orderId, CompleteMaintenanceOrderResource resource) {
        return new CompleteMaintenanceOrderCommand(orderId, resource.closingOdometer());
    }
}
