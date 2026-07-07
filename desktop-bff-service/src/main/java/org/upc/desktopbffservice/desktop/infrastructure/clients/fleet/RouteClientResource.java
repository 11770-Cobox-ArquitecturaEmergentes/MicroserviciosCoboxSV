package org.upc.desktopbffservice.desktop.infrastructure.clients.fleet;

import java.util.List;
import java.util.Set;

public record RouteClientResource(
        Long id,
        String title,
        Long vehicleId,
        Long driverId,
        List<OrderIdClientResource> ordersIds,
        Set<OrderIdClientResource> finishedOrderIds,
        String routeStatus
) {
}
