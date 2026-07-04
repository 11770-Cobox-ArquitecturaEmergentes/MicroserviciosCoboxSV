package org.upc.fleetservice.fleet.domain.exceptions;

public class DriverAlreadyExistsException extends RuntimeException {
    public DriverAlreadyExistsException(String field, String value) {
        super("Driver already exists with %s: %s".formatted(field, value));
    }
}
