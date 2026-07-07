package org.upc.fleetservice.fleet.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import org.upc.fleetservice.fleet.domain.model.valueobjects.VehicleStatus;

public record UpdateVehicleStatusResource(
        @NotNull(message = "vehicleStatus cannot be null")
        VehicleStatus vehicleStatus
) {}
