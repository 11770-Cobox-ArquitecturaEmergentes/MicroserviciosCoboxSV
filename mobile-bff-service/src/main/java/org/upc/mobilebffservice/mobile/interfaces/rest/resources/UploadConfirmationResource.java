package org.upc.mobilebffservice.mobile.interfaces.rest.resources;

import org.upc.mobilebffservice.mobile.domain.model.valueobjects.UploadIntentStatus;

import java.time.Instant;
import java.util.UUID;

public record UploadConfirmationResource(
        UUID uploadIntentId,
        UUID clientEvidenceId,
        String objectKey,
        UploadIntentStatus status,
        Instant confirmedAt
) {
}
