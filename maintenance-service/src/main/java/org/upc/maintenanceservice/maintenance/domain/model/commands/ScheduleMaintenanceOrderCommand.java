package org.upc.maintenanceservice.maintenance.domain.model.commands;

public record ScheduleMaintenanceOrderCommand(Long orderId, Integer scheduledTimelapseDays) {
}
