package org.upc.maintenanceservice.maintenance.interfaces.rest.transform;

import org.upc.maintenanceservice.maintenance.domain.model.entities.Job;
import org.upc.maintenanceservice.maintenance.interfaces.rest.resources.JobResource;

public class JobResourceFromEntityAssembler {
    public static JobResource toResourceFromEntity(Job entity) {
        return new JobResource(entity.getId(), entity.getDescription(), entity.isCompleted());
    }
}
