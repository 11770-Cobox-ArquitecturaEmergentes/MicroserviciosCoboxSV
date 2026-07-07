package org.upc.desktopbffservice.desktop.infrastructure.clients.delivery;

public record OrderClientResource(
        Long id,
        Long clientId,
        String addressLine,
        String city,
        String country,
        String postalCode,
        Double referenceLatitude,
        Double referenceLongitude,
        String notes,
        Double weightKg,
        String orderStatus
) {
}
