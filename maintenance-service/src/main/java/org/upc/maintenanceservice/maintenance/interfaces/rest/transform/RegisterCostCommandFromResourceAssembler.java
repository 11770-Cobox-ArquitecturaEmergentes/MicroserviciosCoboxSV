package org.upc.maintenanceservice.maintenance.interfaces.rest.transform;

import org.upc.maintenanceservice.maintenance.domain.model.commands.RegisterCostCommand;
import org.upc.maintenanceservice.maintenance.interfaces.rest.resources.RegisterCostResource;

public class RegisterCostCommandFromResourceAssembler {
    public static RegisterCostCommand toCommandFromResource(Long orderId, RegisterCostResource resource) {
        return new RegisterCostCommand(orderId, resource.amount(), resource.currency());
    }
}
