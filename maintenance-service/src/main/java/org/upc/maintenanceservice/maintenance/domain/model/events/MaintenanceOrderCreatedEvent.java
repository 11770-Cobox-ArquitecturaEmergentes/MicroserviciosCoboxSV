package org.upc.maintenanceservice.maintenance.domain.model.events;

public record MaintenanceOrderCreatedEvent(Long orderId, Long vehicleId) {
}
