package org.upc.aivalidationservice.validation.infrastructure.messaging;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cobox.rabbitmq")
public record RabbitMessagingProperties(
        String exchange,
        String evidenceUploadConfirmedRoutingKey,
        String evidenceUploadConfirmedQueue,
        String evidenceUploadConfirmedDlq
) {
    public RabbitMessagingProperties {
        exchange = exchange == null ? "cobox.events" : exchange;
        evidenceUploadConfirmedRoutingKey = evidenceUploadConfirmedRoutingKey == null
                ? "evidence.upload.confirmed"
                : evidenceUploadConfirmedRoutingKey;
        evidenceUploadConfirmedQueue = evidenceUploadConfirmedQueue == null
                ? "ai.evidence-upload-confirmed"
                : evidenceUploadConfirmedQueue;
        evidenceUploadConfirmedDlq = evidenceUploadConfirmedDlq == null
                ? "ai.evidence-upload-confirmed.dlq"
                : evidenceUploadConfirmedDlq;
    }
}
