package org.upc.mobilebffservice.mobile.application.internal.outbox;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.upc.mobilebffservice.mobile.domain.model.entities.OutboxMessage;
import org.upc.mobilebffservice.mobile.domain.model.valueobjects.OutboxStatus;
import org.upc.mobilebffservice.mobile.infrastructure.messaging.RabbitMessagingProperties;
import org.upc.mobilebffservice.mobile.infrastructure.persistence.jpa.repositories.OutboxMessageRepository;

import java.time.Instant;
import java.util.List;

@Service
public class EvidenceUploadOutboxRelay {

    private final OutboxMessageRepository outboxMessageRepository;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitMessagingProperties properties;

    public EvidenceUploadOutboxRelay(OutboxMessageRepository outboxMessageRepository,
                                     RabbitTemplate rabbitTemplate,
                                     RabbitMessagingProperties properties) {
        this.outboxMessageRepository = outboxMessageRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${cobox.rabbitmq.relay-interval-ms:5000}")
    @Transactional
    public void publishPendingEvidenceEvents() {
        var candidates = outboxMessageRepository.findTop20ByStatusInOrderByOccurredAtAsc(
                List.of(OutboxStatus.PENDING, OutboxStatus.FAILED)
        );
        candidates.forEach(this::publish);
    }

    private void publish(OutboxMessage message) {
        if (message.getRetryCount() != null && message.getRetryCount() >= properties.relayMaxRetries()) {
            message.markDeadLettered("max retries exceeded before publish", Instant.now());
            outboxMessageRepository.save(message);
            return;
        }

        try {
            message.markProcessing();
            outboxMessageRepository.save(message);
            rabbitTemplate.convertAndSend(
                    properties.exchange(),
                    properties.evidenceUploadConfirmedRoutingKey(),
                    message.getPayload()
            );
            message.markPublished(Instant.now());
        } catch (RuntimeException ex) {
            message.markFailed(ex.getMessage());
            if (message.getRetryCount() >= properties.relayMaxRetries()) {
                message.markDeadLettered(ex.getMessage(), Instant.now());
            }
        }
        outboxMessageRepository.save(message);
    }
}
