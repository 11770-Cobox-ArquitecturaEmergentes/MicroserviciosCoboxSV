package org.upc.desktopbffservice.desktop.interfaces.rest.resources;

import java.time.Instant;
import java.util.List;

public record VehicleHealthResource(
        Instant generatedAt,
        VehicleSummaryResource vehicle,
        List<MaintenanceOrderSummaryResource> openMaintenanceOrders,
        List<MaintenanceOrderSummaryResource> maintenanceHistory,
        List<DegradedSectionResource> degradedSections
) {
}
