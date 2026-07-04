package org.upc.maintenanceservice.maintenance.domain.model.queries;

import org.upc.maintenanceservice.maintenance.domain.model.valueobjects.MaintenanceOrderStatus;

public record GetMaintenanceOrdersByStatusQuery(MaintenanceOrderStatus status) {
}
