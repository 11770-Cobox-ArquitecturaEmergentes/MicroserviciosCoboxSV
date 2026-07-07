package org.upc.maintenanceservice.maintenance.interfaces.rest.resources;

public record MaintenanceRuleResource(String name, Long thresholdKm, Integer thresholdDays) {
}
