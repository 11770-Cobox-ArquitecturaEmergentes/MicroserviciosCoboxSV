package org.upc.maintenanceservice.maintenance.domain.exceptions;

public class InvalidMaintenanceCurrencyException extends RuntimeException {
    public InvalidMaintenanceCurrencyException(String currentCurrency, String targetCurrency) {
        super("Cannot register maintenance cost in currency " + targetCurrency + " because current currency is " + currentCurrency);
    }
}
