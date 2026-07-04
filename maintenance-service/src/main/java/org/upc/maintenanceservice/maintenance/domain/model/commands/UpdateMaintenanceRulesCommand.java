package org.upc.maintenanceservice.maintenance.domain.model.commands;

import org.upc.maintenanceservice.maintenance.domain.model.valueobjects.MaintenanceRule;

import java.util.List;

public record UpdateMaintenanceRulesCommand(Long scheduleId, List<MaintenanceRule> rules) {
}
