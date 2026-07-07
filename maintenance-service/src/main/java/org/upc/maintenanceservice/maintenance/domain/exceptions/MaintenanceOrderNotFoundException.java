package org.upc.maintenanceservice.maintenance.domain.exceptions;

public class MaintenanceOrderNotFoundException extends RuntimeException {
    public MaintenanceOrderNotFoundException(Object id) {
        super("Maintenance order not found with identifier: " + id);
    }
}
