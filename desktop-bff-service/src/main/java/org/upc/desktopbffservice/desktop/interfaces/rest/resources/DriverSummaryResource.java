package org.upc.desktopbffservice.desktop.interfaces.rest.resources;

public record DriverSummaryResource(
        Long id,
        String email,
        String licenceNumber,
        String status
) {
}
