package org.upc.desktopbffservice.desktop.interfaces.rest.resources;

public record OrderSummaryResource(
        Long id,
        Long clientId,
        String city,
        String country,
        Double weightKg,
        String status
) {
}
