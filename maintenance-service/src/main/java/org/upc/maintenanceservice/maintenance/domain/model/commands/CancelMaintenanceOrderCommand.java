package org.upc.maintenanceservice.maintenance.domain.model.commands;

public record CancelMaintenanceOrderCommand(Long orderId, String reason) {
}
