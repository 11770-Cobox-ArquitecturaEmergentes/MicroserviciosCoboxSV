package org.upc.mobilebffservice.mobile.domain.exceptions;

import java.util.UUID;

public class UploadIntentNotFoundException extends RuntimeException {
    public UploadIntentNotFoundException(UUID uploadIntentId) {
        super("Upload intent not found: " + uploadIntentId);
    }
}
