package org.upc.fleetservice.fleet.interfaces.rest.transform;

import org.upc.fleetservice.fleet.domain.model.commands.UpdateVehicleStatusCommand;
import org.upc.fleetservice.fleet.interfaces.rest.resources.UpdateVehicleStatusResource;

public class UpdateVehicleStatusCommandFromResourceAssembler {
    public static UpdateVehicleStatusCommand toCommandFromResource(Long vehicleId, UpdateVehicleStatusResource resource) {
        return new UpdateVehicleStatusCommand(vehicleId, resource.vehicleStatus());
    }
}
