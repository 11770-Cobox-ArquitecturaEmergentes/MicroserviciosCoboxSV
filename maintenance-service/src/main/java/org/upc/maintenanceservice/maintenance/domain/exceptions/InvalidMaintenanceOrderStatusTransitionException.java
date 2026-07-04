package org.upc.maintenanceservice.maintenance.domain.exceptions;

import org.upc.maintenanceservice.maintenance.domain.model.valueobjects.MaintenanceOrderStatus;

public class InvalidMaintenanceOrderStatusTransitionException extends RuntimeException {
    public InvalidMaintenanceOrderStatusTransitionException(MaintenanceOrderStatus currentStatus, MaintenanceOrderStatus targetStatus) {
        super("Cannot transition maintenance order status from " + currentStatus + " to " + targetStatus);
    }
}
