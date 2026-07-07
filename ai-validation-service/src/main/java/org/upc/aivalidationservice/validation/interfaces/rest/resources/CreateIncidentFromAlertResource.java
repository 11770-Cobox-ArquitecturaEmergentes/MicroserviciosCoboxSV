package org.upc.aivalidationservice.validation.interfaces.rest.resources;

public record CreateIncidentFromAlertResource(
        String type,
        String description,
        String severity,
        Long responsibleUserId
) {
}
