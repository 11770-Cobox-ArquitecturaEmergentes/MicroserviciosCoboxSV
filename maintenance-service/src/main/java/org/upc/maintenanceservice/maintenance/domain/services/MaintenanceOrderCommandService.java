package org.upc.maintenanceservice.maintenance.domain.services;

import org.upc.maintenanceservice.maintenance.domain.model.commands.*;

public interface MaintenanceOrderCommandService {
    Long handle(CreateMaintenanceOrderCommand command);

    void handle(ScheduleMaintenanceOrderCommand command);

    void handle(StartMaintenanceOrderCommand command);

    void handle(CompleteMaintenanceOrderCommand command);

    void handle(CancelMaintenanceOrderCommand command);

    void handle(RegisterJobCommand command);

    void handle(RequestPartsCommand command);

    void handle(ReceivePartsCommand command);

    void handle(RegisterCostCommand command);
}
