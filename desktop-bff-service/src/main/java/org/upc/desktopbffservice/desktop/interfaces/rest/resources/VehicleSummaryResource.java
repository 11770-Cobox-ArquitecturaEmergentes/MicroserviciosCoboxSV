package org.upc.desktopbffservice.desktop.interfaces.rest.resources;

public record VehicleSummaryResource(
        Long id,
        String plateNumber,
        Double capacityKg,
        String status
) {
}
