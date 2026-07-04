package org.upc.maintenanceservice.maintenance.domain.model.commands;

public record RequestPartsCommand(Long orderId, String partName, Integer quantity) {
}
