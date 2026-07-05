package org.upc.mobilebffservice.mobile.application.internal.commandservices;

import org.upc.mobilebffservice.mobile.interfaces.rest.resources.ConfirmUploadIntentResource;
import org.upc.mobilebffservice.mobile.interfaces.rest.resources.CreateUploadIntentResource;
import org.upc.mobilebffservice.mobile.interfaces.rest.resources.UploadConfirmationResource;
import org.upc.mobilebffservice.mobile.interfaces.rest.resources.UploadIntentResource;

import java.util.UUID;

public interface UploadIntentCommandService {
    UploadIntentResource create(CreateUploadIntentResource resource, Long driverId);
    UploadConfirmationResource confirm(UUID uploadIntentId, ConfirmUploadIntentResource resource);
}
