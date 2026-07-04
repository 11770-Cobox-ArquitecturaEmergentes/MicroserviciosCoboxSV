package org.upc.fleetservice.fleet.domain.model.aggregates;



import jakarta.persistence.*;
import lombok.Getter;
import org.upc.fleetservice.fleet.domain.exceptions.DriverNotInRouteException;
import org.upc.fleetservice.fleet.domain.model.commands.CreateDriverCommand;
import org.upc.fleetservice.fleet.domain.model.valueobjects.DriverStatus;
import org.upc.fleetservice.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

@Entity
@Getter
public class Driver extends AuditableAbstractAggregateRoot<Driver> {

    @Column(unique = true)
    private String email;

    private DriverStatus driverStatus;
    private String licenceNumber;

    public Driver() {

    }
    public Driver(CreateDriverCommand command) {
        this.email = command.email();
        this.licenceNumber = command.licenceNumber();
        this.driverStatus = DriverStatus.AVAILABLE;
    }

    /**
     * Assigns the driver to a route.
     */
    public void markAsInRoute() {
        if (this.driverStatus != DriverStatus.AVAILABLE) {
            throw new IllegalStateException("Driver is not available for a new route.");
        }
        this.driverStatus = DriverStatus.ON_ROUTE;
    }

    /**
     * Frees the driver after completing a route.
     */
    public void returnFromRoute() {
        if (this.driverStatus != DriverStatus.ON_ROUTE) {
            throw new DriverNotInRouteException(this.driverStatus);
        }
        this.driverStatus = DriverStatus.AVAILABLE;
    }

    /**
     * Sets the driver's driverStatus to a short break.
     */
    public void takeBreak() {
        if (this.driverStatus == DriverStatus.ON_ROUTE) {
            this.driverStatus = DriverStatus.ON_BREAK;
        }
    }

    public boolean hasAvailable() {
        return this.driverStatus == DriverStatus.AVAILABLE;
    }

    /**
     * Marks the driver as unavailable for duty.
     */
    public void makeUnavailable() {
        this.driverStatus = DriverStatus.UNAVAILABLE;
    }

}
