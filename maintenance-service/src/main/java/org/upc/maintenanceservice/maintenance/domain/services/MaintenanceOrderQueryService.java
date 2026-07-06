package org.upc.maintenanceservice.maintenance.domain.services;

import org.upc.maintenanceservice.maintenance.domain.model.aggregates.MaintenanceOrder;
import org.upc.maintenanceservice.maintenance.domain.model.queries.*;

import java.util.List;
import java.util.Optional;

public interface MaintenanceOrderQueryService {
    Optional<MaintenanceOrder> handle(GetMaintenanceOrderByIdQuery query);

    List<MaintenanceOrder> handle(GetMaintenanceOrderHistoryQuery query);

    List<MaintenanceOrder> handle(GetOpenMaintenanceOrdersByVehicleIdQuery query);

    List<MaintenanceOrder> handle(GetMaintenanceOrdersByStatusQuery query);

    boolean handle(HasMaintenanceOpenOrderForVehicleIdQuery query);

    List<MaintenanceOrder> handle(GetAllMaintenanceOrdersQuery query);
}
