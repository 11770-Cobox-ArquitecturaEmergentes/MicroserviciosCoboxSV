package org.upc.desktopbffservice.desktop.infrastructure.clients.maintenance;

public record MaintenanceRuleClientResource(
        String name,
        Long thresholdKm,
        Integer thresholdDays
) {
}
