package org.upc.desktopbffservice.desktop.interfaces.rest.resources;

import java.util.List;

public record SmartVisionAnalysisOverviewResource(
        SmartVisionAnalysisResource analysis,
        List<SmartVisionAlertResource> alerts,
        DriverSummaryResource driver,
        RouteSummaryResource route,
        VehicleSummaryResource vehicle,
        OrderSummaryResource order,
        List<DegradedSectionResource> degradedSections
) {
}
