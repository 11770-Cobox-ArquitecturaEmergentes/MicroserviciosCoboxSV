package org.upc.maintenanceservice.maintenance.interfaces.rest.transform;

import org.upc.maintenanceservice.maintenance.domain.model.commands.StartMaintenanceOrderCommand;

public class StartMaintenanceOrderCommandFromResourceAssembler {
    public static StartMaintenanceOrderCommand toCommandFromResource(Long orderId) {
        return new StartMaintenanceOrderCommand(orderId);
    }
}
