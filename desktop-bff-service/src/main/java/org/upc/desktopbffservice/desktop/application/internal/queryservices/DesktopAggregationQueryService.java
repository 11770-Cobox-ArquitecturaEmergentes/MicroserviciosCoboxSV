package org.upc.desktopbffservice.desktop.application.internal.queryservices;

import org.upc.desktopbffservice.desktop.interfaces.rest.resources.OperationsDashboardResource;
import org.upc.desktopbffservice.desktop.interfaces.rest.resources.RouteOverviewResource;
import org.upc.desktopbffservice.desktop.interfaces.rest.resources.VehicleHealthResource;

public interface DesktopAggregationQueryService {
    OperationsDashboardResource getOperationsDashboard();
    RouteOverviewResource getRouteOverview(Long routeId);
    VehicleHealthResource getVehicleHealth(Long vehicleId);
}
