package org.upc.maintenanceservice.maintenance.domain.exceptions;

public class MaintenanceScheduleNotFoundException extends RuntimeException {
    public MaintenanceScheduleNotFoundException(Object id) {
        super("Maintenance schedule not found with identifier: " + id);
    }
}
