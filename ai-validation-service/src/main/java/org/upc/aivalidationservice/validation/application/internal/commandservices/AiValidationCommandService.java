package org.upc.aivalidationservice.validation.application.internal.commandservices;

import org.upc.aivalidationservice.validation.interfaces.messaging.EvidenceUploadConfirmedEvent;
import org.upc.aivalidationservice.validation.interfaces.rest.resources.AiAlertResource;
import org.upc.aivalidationservice.validation.interfaces.rest.resources.CreateIncidentFromAlertResource;
import org.upc.aivalidationservice.validation.interfaces.rest.resources.IncidentFromAlertResource;

import java.util.UUID;

public interface AiValidationCommandService {
    void handleEvidenceUploadConfirmed(EvidenceUploadConfirmedEvent event);
    AiAlertResource acknowledgeAlert(UUID alertId);
    AiAlertResource resolveAlert(UUID alertId, String notes);
    IncidentFromAlertResource createIncidentFromAlert(UUID alertId, CreateIncidentFromAlertResource resource);
}
