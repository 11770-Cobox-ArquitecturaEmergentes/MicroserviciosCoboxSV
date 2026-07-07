package org.upc.aivalidationservice.validation.application.internal.providers;

import java.util.List;

public record AiVisionResult(
        String provider,
        String ocrText,
        List<String> labels,
        double confidenceScore,
        boolean lowQuality,
        boolean ambiguous,
        boolean illegible
) {
}
