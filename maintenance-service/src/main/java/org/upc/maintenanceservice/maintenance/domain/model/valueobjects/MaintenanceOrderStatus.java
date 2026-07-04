package org.upc.maintenanceservice.maintenance.domain.model.valueobjects;

public enum MaintenanceOrderStatus {
    OPEN,
    SCHEDULED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;

    public boolean canTransitionTo(MaintenanceOrderStatus targetStatus) {
        return switch (this) {
            case OPEN -> targetStatus == SCHEDULED || targetStatus == CANCELLED;
            case SCHEDULED -> targetStatus == IN_PROGRESS || targetStatus == CANCELLED;
            case IN_PROGRESS -> targetStatus == COMPLETED || targetStatus == CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };
    }
}
