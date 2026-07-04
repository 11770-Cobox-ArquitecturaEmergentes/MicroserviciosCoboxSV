package org.upc.maintenanceservice.maintenance.domain.exceptions;

public class DuplicateOpenMaintenanceOrderException extends RuntimeException {
    public DuplicateOpenMaintenanceOrderException(Long vehicleId) {
        super("There is already an open maintenance order for vehicle " + vehicleId);
    }
}
