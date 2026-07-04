package org.upc.fleetservice.fleet.interfaces.rest.transform;

import org.upc.fleetservice.fleet.domain.model.aggregates.Driver;

import org.upc.fleetservice.fleet.interfaces.rest.resources.DriverResource;

public class DriverResourceFromEntityAssembler {
    public static DriverResource toResourceFromEntity(Driver entity){
        return new DriverResource(
                entity.getId(),
                entity.getEmail(),
                entity.getLicenceNumber(),
                entity.getDriverStatus().name()
        );
    }
}