package org.upc.maintenanceservice.maintenance.interfaces.rest.transform;

import org.upc.maintenanceservice.maintenance.domain.model.commands.RequestPartsCommand;
import org.upc.maintenanceservice.maintenance.interfaces.rest.resources.RequestPartsResource;

public class RequestPartsCommandFromResourceAssembler {
    public static RequestPartsCommand toCommandFromResource(Long orderId, RequestPartsResource resource) {
        return new RequestPartsCommand(orderId, resource.partName(), resource.quantity());
    }
}
