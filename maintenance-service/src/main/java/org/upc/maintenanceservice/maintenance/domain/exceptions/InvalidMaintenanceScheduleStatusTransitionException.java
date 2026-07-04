package org.upc.maintenanceservice.maintenance.domain.exceptions;

import org.upc.maintenanceservice.maintenance.domain.model.valueobjects.MaintenanceScheduleStatus;

public class InvalidMaintenanceScheduleStatusTransitionException extends RuntimeException {
    public InvalidMaintenanceScheduleStatusTransitionException(MaintenanceScheduleStatus currentStatus, MaintenanceScheduleStatus targetStatus) {
        super("Cannot transition maintenance schedule status from " + currentStatus + " to " + targetStatus);
    }
}
