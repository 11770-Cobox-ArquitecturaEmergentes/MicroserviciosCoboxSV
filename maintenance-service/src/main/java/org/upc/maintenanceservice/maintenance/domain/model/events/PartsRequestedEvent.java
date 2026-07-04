package org.upc.maintenanceservice.maintenance.domain.model.events;

public record PartsRequestedEvent(Long orderId, String partName, Integer quantity) {
}
