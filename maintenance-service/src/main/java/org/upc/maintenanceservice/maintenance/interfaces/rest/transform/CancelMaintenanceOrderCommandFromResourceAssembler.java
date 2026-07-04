package org.upc.maintenanceservice.maintenance.interfaces.rest.transform;

import org.upc.maintenanceservice.maintenance.domain.model.commands.CancelMaintenanceOrderCommand;
import org.upc.maintenanceservice.maintenance.interfaces.rest.resources.CancelMaintenanceOrderResource;

public class CancelMaintenanceOrderCommandFromResourceAssembler {
    public static CancelMaintenanceOrderCommand toCommandFromResource(Long orderId, CancelMaintenanceOrderResource resource) {
        return new CancelMaintenanceOrderCommand(orderId, resource.reason());
    }
}
