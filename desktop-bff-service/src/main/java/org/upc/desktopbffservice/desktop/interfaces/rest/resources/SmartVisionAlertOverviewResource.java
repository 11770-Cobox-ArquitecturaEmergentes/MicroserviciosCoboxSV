package org.upc.desktopbffservice.desktop.interfaces.rest.resources;

import java.util.List;

public record SmartVisionAlertOverviewResource(
        SmartVisionAlertResource alert,
        SmartVisionAnalysisResource analysis,
        DriverSummaryResource driver,
        RouteSummaryResource route,
        VehicleSummaryResource vehicle,
        OrderSummaryResource order,
        List<DegradedSectionResource> degradedSections
) {
}
