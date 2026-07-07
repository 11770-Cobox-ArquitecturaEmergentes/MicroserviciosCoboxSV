package org.upc.fleetservice.fleet.domain.exceptions;

import org.upc.fleetservice.fleet.domain.model.valueobjects.VehicleStatus;

public class InvalidVehicleStateTransitionException extends RuntimeException {

    public InvalidVehicleStateTransitionException(VehicleStatus vehicleStatus) {
        super("Invalid status transition for vehicle in its current state: " + vehicleStatus);
    }
}

