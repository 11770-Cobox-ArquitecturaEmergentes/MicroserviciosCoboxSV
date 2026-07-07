package org.upc.aivalidationservice.validation.interfaces.rest.resources;

import java.util.UUID;

public record IncidentFromAlertResource(
        UUID alertId,
        UUID clientEvidenceId,
        UUID incidentId,
        boolean created
) {
}
