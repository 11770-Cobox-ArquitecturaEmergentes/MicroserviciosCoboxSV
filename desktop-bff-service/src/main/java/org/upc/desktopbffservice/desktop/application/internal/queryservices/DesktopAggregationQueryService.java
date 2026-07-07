package org.upc.desktopbffservice.desktop.application.internal.queryservices;

import org.upc.desktopbffservice.desktop.interfaces.rest.resources.OperationsDashboardResource;
import org.upc.desktopbffservice.desktop.interfaces.rest.resources.RouteOverviewResource;
import org.upc.desktopbffservice.desktop.interfaces.rest.resources.SmartVisionAlertOverviewResource;
import org.upc.desktopbffservice.desktop.interfaces.rest.resources.SmartVisionAnalysisOverviewResource;
import org.upc.desktopbffservice.desktop.interfaces.rest.resources.VehicleHealthResource;

import java.util.List;

public interface DesktopAggregationQueryService {
    OperationsDashboardResource getOperationsDashboard();
    RouteOverviewResource getRouteOverview(Long routeId);
    VehicleHealthResource getVehicleHealth(Long vehicleId);
    List<SmartVisionAlertOverviewResource> getSmartVisionAlerts(String status);
    List<SmartVisionAnalysisOverviewResource> getSmartVisionAnalyses(String status, Long driverId, Long routeId, Long orderId);
}
