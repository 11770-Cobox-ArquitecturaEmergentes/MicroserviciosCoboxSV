package org.upc.maintenanceservice.maintenance.domain.exceptions;

public class InvalidMaintenanceRulesException extends RuntimeException {
    public InvalidMaintenanceRulesException() {
        super("Maintenance rules cannot be empty");
    }
}
