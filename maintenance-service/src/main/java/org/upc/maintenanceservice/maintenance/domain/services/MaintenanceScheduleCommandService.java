package org.upc.maintenanceservice.maintenance.domain.services;

import org.upc.maintenanceservice.maintenance.domain.model.commands.*;

public interface MaintenanceScheduleCommandService {
    Long handle(CreateMaintenanceScheduleCommand command);

    void handle(ActivateMaintenanceScheduleCommand command);

    void handle(DeactivateMaintenanceScheduleCommand command);

    void handle(EvaluateMaintenanceScheduleCommand command);

    void handle(UpdateMaintenanceRulesCommand command);
}
