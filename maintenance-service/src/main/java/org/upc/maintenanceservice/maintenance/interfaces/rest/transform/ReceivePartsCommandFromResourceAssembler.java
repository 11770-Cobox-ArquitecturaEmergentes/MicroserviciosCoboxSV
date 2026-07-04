package org.upc.maintenanceservice.maintenance.interfaces.rest.transform;

import org.upc.maintenanceservice.maintenance.domain.model.commands.ReceivePartsCommand;
import org.upc.maintenanceservice.maintenance.interfaces.rest.resources.ReceivePartsResource;

public class ReceivePartsCommandFromResourceAssembler {
    public static ReceivePartsCommand toCommandFromResource(Long orderId, ReceivePartsResource resource) {
        return new ReceivePartsCommand(orderId, resource.partsRequestId());
    }
}
