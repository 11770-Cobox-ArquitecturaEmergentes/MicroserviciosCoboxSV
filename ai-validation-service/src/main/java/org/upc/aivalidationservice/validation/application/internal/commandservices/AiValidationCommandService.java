package org.upc.aivalidationservice.validation.application.internal.commandservices;

import org.upc.aivalidationservice.validation.interfaces.messaging.EvidenceUploadConfirmedEvent;

public interface AiValidationCommandService {
    void handleEvidenceUploadConfirmed(EvidenceUploadConfirmedEvent event);
}
