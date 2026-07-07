package org.upc.fleetservice.fleet.domain.model.aggregates;

import jakarta.persistence.*;
import lombok.Getter;
import org.upc.fleetservice.fleet.domain.exceptions.InvalidVehicleStateTransitionException;
import org.upc.fleetservice.fleet.domain.exceptions.VehicleNotInRouteException;
import org.upc.fleetservice.fleet.domain.exceptions.VehicleNotOperationalException;
import org.upc.fleetservice.fleet.domain.model.commands.CreateVehicleCommand;
import org.upc.fleetservice.fleet.domain.model.valueobjects.VehicleStatus;
import org.upc.fleetservice.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

@Entity
@Getter
public class Vehicle extends AuditableAbstractAggregateRoot<Vehicle>{


    private String plateNumber;
    private Double capacityKg;
    private VehicleStatus vehicleStatus;

    public Vehicle() {
    }
    public Vehicle(CreateVehicleCommand command) {
        this.plateNumber = command.plateNumber();
        this.capacityKg = command.capacityKg();
        this.vehicleStatus = VehicleStatus.OPERATIONAL;
    }


    public void markAsInRoute() {
        if (this.vehicleStatus != VehicleStatus.OPERATIONAL) {
            throw new VehicleNotOperationalException(this.vehicleStatus);
        }
        this.vehicleStatus = VehicleStatus.ON_ROUTE;
    }


    public boolean hasCapacityFor(double totalWeightKg) {
        return totalWeightKg <= capacityKg;
    }


    /**
     * Frees the vehicle upon route completion.
     */
    public void returnFromRoute() {
        if (this.vehicleStatus != VehicleStatus.ON_ROUTE) throw new VehicleNotInRouteException(this.vehicleStatus);
        this.vehicleStatus = VehicleStatus.OPERATIONAL;
    }

    /**
     * Updates the vehicle status to the provided value.
     * Allowed target statuses: OPERATIONAL, IN_MAINTENANCE, OUT_OF_SERVICE.
     * A vehicle currently ON_ROUTE cannot be updated through this method.
     */
    public void updateStatus(VehicleStatus newStatus) {
        if (newStatus == VehicleStatus.ON_ROUTE) {
            throw new InvalidVehicleStateTransitionException(newStatus);
        }
        if (this.vehicleStatus == VehicleStatus.ON_ROUTE) {
            throw new InvalidVehicleStateTransitionException(this.vehicleStatus);
        }
        this.vehicleStatus = newStatus;
    }

}
