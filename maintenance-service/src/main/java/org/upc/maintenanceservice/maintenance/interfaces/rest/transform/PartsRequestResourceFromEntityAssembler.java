package org.upc.maintenanceservice.maintenance.interfaces.rest.transform;

import org.upc.maintenanceservice.maintenance.domain.model.entities.PartsRequest;
import org.upc.maintenanceservice.maintenance.interfaces.rest.resources.PartsRequestResource;

public class PartsRequestResourceFromEntityAssembler {
    public static PartsRequestResource toResourceFromEntity(PartsRequest entity) {
        return new PartsRequestResource(
                entity.getId(),
                entity.getPartName(),
                entity.getQuantity() != null ? entity.getQuantity().quantity() : null,
                entity.getStatus() != null ? entity.getStatus().name() : null
        );
    }
}
