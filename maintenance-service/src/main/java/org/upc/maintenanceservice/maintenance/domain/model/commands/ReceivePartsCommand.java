package org.upc.maintenanceservice.maintenance.domain.model.commands;

public record ReceivePartsCommand(Long orderId, Long partsRequestId) {
}
