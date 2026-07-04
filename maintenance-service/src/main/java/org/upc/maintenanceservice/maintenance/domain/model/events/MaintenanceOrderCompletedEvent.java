package org.upc.maintenanceservice.maintenance.domain.model.events;

public record MaintenanceOrderCompletedEvent(Long orderId, Long closingOdometer) {
}
