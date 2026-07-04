package org.upc.maintenanceservice.maintenance.domain.model.commands;

public record RegisterJobCommand(Long orderId, String description, Boolean completed) {
}
