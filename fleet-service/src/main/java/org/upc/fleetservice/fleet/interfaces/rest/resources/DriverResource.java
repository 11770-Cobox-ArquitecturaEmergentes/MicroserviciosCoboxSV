package org.upc.fleetservice.fleet.interfaces.rest.resources;

public record DriverResource(
        Long id,
        String email,
        String licenceNumber,
        String driverStatus) {
}
