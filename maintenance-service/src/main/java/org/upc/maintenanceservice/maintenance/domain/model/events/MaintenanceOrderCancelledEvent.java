package org.upc.maintenanceservice.maintenance.domain.model.events;

public record MaintenanceOrderCancelledEvent(Long orderId, String reason) {
}
