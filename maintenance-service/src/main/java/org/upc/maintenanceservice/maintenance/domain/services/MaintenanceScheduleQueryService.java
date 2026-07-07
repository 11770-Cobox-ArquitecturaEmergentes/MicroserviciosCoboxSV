package org.upc.maintenanceservice.maintenance.domain.services;

import org.upc.maintenanceservice.maintenance.domain.model.aggregates.MaintenanceSchedule;
import org.upc.maintenanceservice.maintenance.domain.model.queries.*;

import java.util.List;
import java.util.Optional;

public interface MaintenanceScheduleQueryService {
    Optional<MaintenanceSchedule> handle(GetMaintenanceScheduleByIdQuery query);

    Optional<MaintenanceSchedule> handle(GetMaintenanceScheduleByVehicleIdQuery query);

    List<MaintenanceSchedule> handle(GetMaintenanceScheduleDueSoonQuery query);

    List<MaintenanceSchedule> handle(GetActiveMaintenanceSchedulesQuery query);
}
