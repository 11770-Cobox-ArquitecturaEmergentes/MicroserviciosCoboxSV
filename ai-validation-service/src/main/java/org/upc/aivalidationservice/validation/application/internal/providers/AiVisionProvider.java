package org.upc.aivalidationservice.validation.application.internal.providers;

public interface AiVisionProvider {
    AiVisionResult analyze(AiVisionRequest request);
}
