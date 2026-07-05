package org.upc.desktopbffservice.desktop.interfaces.rest.resources;

import java.time.Instant;
import java.util.List;

public record RouteOverviewResource(
        Instant generatedAt,
        RouteSummaryResource route,
        DriverSummaryResource driver,
        VehicleSummaryResource vehicle,
        List<OrderSummaryResource> orders,
        List<Long> finishedOrderIds,
        List<DegradedSectionResource> degradedSections
) {
}
