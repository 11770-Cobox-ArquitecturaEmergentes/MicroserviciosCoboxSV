package org.upc.desktopbffservice.desktop.interfaces.rest.resources;

import java.util.List;
import java.util.Map;

public record FleetDashboardResource(
        long totalVehicles,
        long totalDrivers,
        long totalRoutes,
        Map<String, Long> vehiclesByStatus,
        Map<String, Long> driversByStatus,
        Map<String, Long> routesByStatus,
        List<RouteSummaryResource> activeRoutes
) {
}
