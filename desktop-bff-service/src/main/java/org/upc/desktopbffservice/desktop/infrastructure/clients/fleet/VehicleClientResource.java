package org.upc.desktopbffservice.desktop.infrastructure.clients.fleet;

public record VehicleClientResource(
        Long id,
        String plateNumber,
        Double capacityKg,
        String vehicleStatus
) {
}
