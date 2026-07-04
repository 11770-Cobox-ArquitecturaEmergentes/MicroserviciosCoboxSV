package org.upc.maintenanceservice.maintenance.interfaces.rest.transform;

import org.upc.maintenanceservice.maintenance.domain.model.commands.RegisterJobCommand;
import org.upc.maintenanceservice.maintenance.interfaces.rest.resources.RegisterJobResource;

public class RegisterJobCommandFromResourceAssembler {
    public static RegisterJobCommand toCommandFromResource(Long orderId, RegisterJobResource resource) {
        return new RegisterJobCommand(orderId, resource.description(), resource.completed());
    }
}
