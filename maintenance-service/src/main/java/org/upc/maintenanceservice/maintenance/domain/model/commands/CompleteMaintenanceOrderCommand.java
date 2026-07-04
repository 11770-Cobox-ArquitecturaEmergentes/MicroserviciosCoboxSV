package org.upc.maintenanceservice.maintenance.domain.model.commands;

public record CompleteMaintenanceOrderCommand(Long orderId, Long closingOdometer) {
}
