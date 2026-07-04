package org.upc.maintenanceservice.maintenance.domain.model.events;

public record PartsReceivedEvent(Long orderId, Long partsRequestId) {
}
