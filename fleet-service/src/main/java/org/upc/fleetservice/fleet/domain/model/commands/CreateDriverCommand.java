package org.upc.fleetservice.fleet.domain.model.commands;

public record CreateDriverCommand(String email, String licenceNumber) {
}
