package org.upc.desktopbffservice.desktop.interfaces.rest.resources;

public record MaintenanceRuleSummaryResource(
        String name,
        Long thresholdKm,
        Integer thresholdDays
) {
}
