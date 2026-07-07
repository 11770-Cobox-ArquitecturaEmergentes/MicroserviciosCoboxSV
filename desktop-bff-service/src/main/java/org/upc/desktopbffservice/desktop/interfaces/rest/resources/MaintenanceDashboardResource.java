package org.upc.desktopbffservice.desktop.interfaces.rest.resources;

import java.util.List;
import java.util.Map;

public record MaintenanceDashboardResource(
        long totalOpenWork,
        Map<String, Long> ordersByStatus,
        List<MaintenanceOrderSummaryResource> openOrders
) {
}
