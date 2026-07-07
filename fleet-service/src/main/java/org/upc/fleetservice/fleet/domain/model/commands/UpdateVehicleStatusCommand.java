package org.upc.fleetservice.fleet.domain.model.commands;

import org.upc.fleetservice.fleet.domain.model.valueobjects.VehicleStatus;

public record UpdateVehicleStatusCommand(Long vehicleId, VehicleStatus newStatus) {
}
