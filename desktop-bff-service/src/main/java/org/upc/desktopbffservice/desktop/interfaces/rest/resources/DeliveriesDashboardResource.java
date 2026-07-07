package org.upc.desktopbffservice.desktop.interfaces.rest.resources;

import java.util.List;
import java.util.Map;

public record DeliveriesDashboardResource(
        long totalOrders,
        Map<String, Long> ordersByStatus,
        List<OrderSummaryResource> recentOrders
) {
}
