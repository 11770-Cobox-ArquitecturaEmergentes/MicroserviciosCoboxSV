package org.upc.aivalidationservice.validation.infrastructure.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.upc.aivalidationservice.validation.application.internal.providers.AiVisionProvider;
import org.upc.aivalidationservice.validation.application.internal.providers.AiVisionRequest;
import org.upc.aivalidationservice.validation.application.internal.providers.AiVisionResult;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.S3Object;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.BlockType;
import software.amazon.awssdk.services.textract.model.DetectDocumentTextRequest;
import software.amazon.awssdk.services.textract.model.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@ConditionalOnProperty(prefix = "cobox.ai", name = "provider", havingValue = "aws", matchIfMissing = true)
public class AwsAiVisionProvider implements AiVisionProvider {

    private static final double MIN_CONFIDENCE = 65.0;

    private final TextractClient textractClient;
    private final RekognitionClient rekognitionClient;

    public AwsAiVisionProvider(TextractClient textractClient, RekognitionClient rekognitionClient) {
        this.textractClient = textractClient;
        this.rekognitionClient = rekognitionClient;
    }

    @Override
    public AiVisionResult analyze(AiVisionRequest request) {
        var ocrText = detectText(request);
        var labels = detectLabels(request);
        var confidence = estimateConfidence(labels, ocrText);
        var lowQuality = hasLowQualitySignal(labels) || confidence < MIN_CONFIDENCE;
        var illegible = ocrText == null || ocrText.isBlank();
        var ambiguous = labels.isEmpty() || confidence < 75.0;
        return new AiVisionResult("AWS_TEXTRACT_REKOGNITION", ocrText, labels, confidence, lowQuality, ambiguous, illegible);
    }

    private String detectText(AiVisionRequest request) {
        var document = Document.builder()
                .s3Object(software.amazon.awssdk.services.textract.model.S3Object.builder()
                        .bucket(request.bucket())
                        .name(request.objectKey())
                        .build())
                .build();
        var response = textractClient.detectDocumentText(DetectDocumentTextRequest.builder()
                .document(document)
                .build());
        return response.blocks().stream()
                .filter(block -> block.blockType() == BlockType.LINE)
                .map(block -> block.text() == null ? "" : block.text())
                .filter(text -> !text.isBlank())
                .reduce((left, right) -> left + "\n" + right)
                .orElse("");
    }

    private List<String> detectLabels(AiVisionRequest request) {
        if ("application/pdf".equalsIgnoreCase(request.mimeType())) {
            return List.of("Document");
        }

        var image = Image.builder()
                .s3Object(S3Object.builder()
                        .bucket(request.bucket())
                        .name(request.objectKey())
                        .build())
                .build();
        var response = rekognitionClient.detectLabels(DetectLabelsRequest.builder()
                .image(image)
                .maxLabels(20)
                .minConfidence(50f)
                .build());
        var labels = new ArrayList<String>();
        response.labels().forEach(label -> labels.add(label.name() + ":" + label.confidence()));
        return labels;
    }

    private double estimateConfidence(List<String> labels, String ocrText) {
        var labelConfidence = labels.stream()
                .map(this::parseConfidence)
                .filter(value -> value > 0)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);
        if (ocrText != null && !ocrText.isBlank()) {
            return labelConfidence == 0 ? 80.0 : Math.max(labelConfidence, 80.0);
        }
        return labelConfidence;
    }

    private double parseConfidence(String label) {
        var parts = label.split(":");
        if (parts.length < 2) return 0;
        try {
            return Double.parseDouble(parts[1]);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private boolean hasLowQualitySignal(List<String> labels) {
        return labels.stream()
                .map(label -> label.toLowerCase(Locale.ROOT))
                .anyMatch(label -> label.contains("blur")
                        || label.contains("dark")
                        || label.contains("low light")
                        || label.contains("poor quality"));
    }
}
