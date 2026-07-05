package org.upc.desktopbffservice.desktop.infrastructure.clients.fleet;

public record DriverClientResource(
        Long id,
        String email,
        String licenceNumber,
        String driverStatus
) {
}
