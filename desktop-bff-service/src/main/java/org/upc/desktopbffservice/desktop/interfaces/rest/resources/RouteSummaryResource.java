package org.upc.desktopbffservice.desktop.interfaces.rest.resources;

import java.util.List;

public record RouteSummaryResource(
        Long id,
        String title,
        Long vehicleId,
        Long driverId,
        List<Long> orderIds,
        List<Long> finishedOrderIds,
        String status
) {
}
