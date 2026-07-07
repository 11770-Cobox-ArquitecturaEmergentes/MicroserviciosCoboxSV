package org.upc.fleetservice.fleet.domain.services;

import org.upc.fleetservice.fleet.domain.model.commands.CreateVehicleCommand;
import org.upc.fleetservice.fleet.domain.model.commands.UpdateVehicleStatusCommand;
import org.upc.fleetservice.fleet.domain.model.commands.UpdateVehicleStatusOnCompletedRouteCommand;

public interface VehicleCommandService {
    Long handle(CreateVehicleCommand command);
    void handle(UpdateVehicleStatusOnCompletedRouteCommand command);
    Long handle(UpdateVehicleStatusCommand command);

}
