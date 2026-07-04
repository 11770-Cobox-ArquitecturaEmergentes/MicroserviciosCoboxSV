package org.upc.maintenanceservice.maintenance.domain.model.events;

public record MaintenanceOrderScheduledEvent(Long orderId, Integer scheduledTimelapseDays) {
}
