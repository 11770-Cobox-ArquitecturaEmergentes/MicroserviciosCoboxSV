package org.upc.fleetservice.fleet.application.internal.commandservices;


import org.springframework.stereotype.Service;

import org.upc.fleetservice.fleet.domain.exceptions.VehicleNotFoundException;
import org.upc.fleetservice.fleet.domain.model.aggregates.Vehicle;
import org.upc.fleetservice.fleet.domain.model.commands.CreateVehicleCommand;
import org.upc.fleetservice.fleet.domain.model.commands.UpdateVehicleStatusCommand;
import org.upc.fleetservice.fleet.domain.model.commands.UpdateVehicleStatusOnCompletedRouteCommand;
import org.upc.fleetservice.fleet.domain.services.VehicleCommandService;
import org.upc.fleetservice.fleet.infrastructure.messaging.FleetReportEventPublisher;
import org.upc.fleetservice.fleet.infrastructure.persistence.jpa.repositories.VehicleRepository;

@Service
public class VehicleCommandServiceImpl implements VehicleCommandService {

    private final VehicleRepository vehicleRepository;
    private final FleetReportEventPublisher reportEventPublisher;
    public VehicleCommandServiceImpl(VehicleRepository vehicleRepository, FleetReportEventPublisher reportEventPublisher) {
        this.vehicleRepository = vehicleRepository;
        this.reportEventPublisher = reportEventPublisher;
    }

    @Override
    public Long handle(CreateVehicleCommand command) {
        var vehicle = new Vehicle(command);
        vehicleRepository.save(vehicle);
        reportEventPublisher.publishVehicle("fleet.vehicle-created", vehicle);
        return vehicle.getId();
    }
    @Override
    public void handle(UpdateVehicleStatusOnCompletedRouteCommand command) {
        vehicleRepository.findById(command.vehicleId()).map(vehicle -> {
            vehicle.returnFromRoute();
            vehicleRepository.save(vehicle);
            reportEventPublisher.publishVehicle("fleet.vehicle-status-updated", vehicle);
            return vehicle.getId();
        }).orElseThrow(() -> new VehicleNotFoundException(command.vehicleId()));
    }
    @Override
    public Long handle(UpdateVehicleStatusCommand command) {
        return vehicleRepository.findById(command.vehicleId()).map(vehicle -> {
            vehicle.updateStatus(command.newStatus());
            vehicleRepository.save(vehicle);
            reportEventPublisher.publishVehicle("fleet.vehicle-status-updated", vehicle);
            return vehicle.getId();
        }).orElseThrow(() -> new VehicleNotFoundException(command.vehicleId()));
    }


}
