package org.upc.mobilebffservice.mobile.infrastructure.storage;

import org.upc.mobilebffservice.mobile.domain.model.aggregates.UploadIntent;

public interface EvidenceStorageService {
    PresignedUpload createPresignedUpload(UploadIntent intent);
    UploadedObjectMetadata inspectObject(String objectKey);
}
