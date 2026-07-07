package org.upc.desktopbffservice.desktop.interfaces.rest.resources;

import java.time.Instant;
import java.util.List;

public record OperationsDashboardResource(
        Instant generatedAt,
        FleetDashboardResource fleet,
        DeliveriesDashboardResource deliveries,
        IncidentsDashboardResource incidents,
        MaintenanceDashboardResource maintenance,
        List<DegradedSectionResource> degradedSections
) {
}
