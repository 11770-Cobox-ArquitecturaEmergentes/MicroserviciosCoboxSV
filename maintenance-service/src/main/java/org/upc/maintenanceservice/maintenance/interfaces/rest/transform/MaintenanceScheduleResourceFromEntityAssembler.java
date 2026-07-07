package org.upc.maintenanceservice.maintenance.interfaces.rest.transform;

import org.upc.maintenanceservice.maintenance.domain.model.aggregates.MaintenanceSchedule;
import org.upc.maintenanceservice.maintenance.interfaces.rest.resources.MaintenanceScheduleResource;

public class MaintenanceScheduleResourceFromEntityAssembler {
    public static MaintenanceScheduleResource toResourceFromEntity(MaintenanceSchedule entity) {
        return new MaintenanceScheduleResource(
                entity.getId(),
                entity.getVehicleId() != null ? entity.getVehicleId().vehicleId() : null,
                entity.getStatus() != null ? entity.getStatus().name() : null,
                entity.getRules().stream().map(MaintenanceRuleResourceFromEntityAssembler::toResourceFromEntity).toList(),
                entity.getLastEvaluationAt(),
                entity.getNextEvaluationAt()
        );
    }
}
