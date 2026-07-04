package org.upc.maintenanceservice.maintenance.interfaces.rest.transform;

import org.upc.maintenanceservice.maintenance.domain.model.aggregates.MaintenanceOrder;
import org.upc.maintenanceservice.maintenance.interfaces.rest.resources.MaintenanceOrderResource;

public class MaintenanceOrderResourceFromEntityAssembler {
    public static MaintenanceOrderResource toResourceFromEntity(MaintenanceOrder entity) {
        return new MaintenanceOrderResource(
                entity.getId(),
                entity.getVehicleId() != null ? entity.getVehicleId().vehicleId() : null,
                entity.getMaintenanceType() != null ? entity.getMaintenanceType().name() : null,
                entity.getPriority() != null ? entity.getPriority().name() : null,
                entity.getStatus() != null ? entity.getStatus().name() : null,
                entity.getReason() != null ? entity.getReason().name() : null,
                entity.getOpeningOdometer() != null ? entity.getOpeningOdometer() : null,
                entity.getClosingOdometer() != null ? entity.getClosingOdometer() : null,
                entity.getScheduledTimelapse() != null ? entity.getScheduledTimelapse().days() : null,
                entity.getJobs().stream().map(JobResourceFromEntityAssembler::toResourceFromEntity).toList(),
                entity.getPartsRequests().stream().map(PartsRequestResourceFromEntityAssembler::toResourceFromEntity).toList(),
                entity.getTotalCost() != null ? entity.getTotalCost().amount() : null,
                entity.getTotalCost() != null ? entity.getTotalCost().currency() : null,
                entity.getTechnicianId()
        );
    }
}
