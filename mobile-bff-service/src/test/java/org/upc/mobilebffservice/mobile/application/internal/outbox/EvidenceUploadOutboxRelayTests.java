package org.upc.mobilebffservice.mobile.application.internal.outbox;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.upc.mobilebffservice.mobile.domain.model.entities.OutboxMessage;
import org.upc.mobilebffservice.mobile.domain.model.valueobjects.OutboxStatus;
import org.upc.mobilebffservice.mobile.infrastructure.messaging.RabbitMessagingProperties;
import org.upc.mobilebffservice.mobile.infrastructure.persistence.jpa.repositories.OutboxMessageRepository;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EvidenceUploadOutboxRelayTests {

    @Test
    void messageExceedingMaxRetriesIsMarkedDeadLettered() {
        var repository = mock(OutboxMessageRepository.class);
        var rabbitTemplate = mock(RabbitTemplate.class);
        var properties = new RabbitMessagingProperties(
                "cobox.events",
                "evidence.upload.confirmed",
                "ai.evidence-upload-confirmed",
                "ai.evidence-upload-confirmed.dlq",
                3
        );
        var relay = new EvidenceUploadOutboxRelay(repository, rabbitTemplate, properties);
        var message = new OutboxMessage("EvidenceUploadConfirmed", "client-evidence-id", "{}", Instant.now());
        message.markFailed("first");
        message.markFailed("second");
        message.markFailed("third");
        when(repository.findTop20ByStatusInOrderByOccurredAtAsc(List.of(OutboxStatus.PENDING, OutboxStatus.FAILED)))
                .thenReturn(List.of(message));

        relay.publishPendingEvidenceEvents();

        assertThat(message.getStatus()).isEqualTo(OutboxStatus.DEAD_LETTERED);
        verify(repository).save(message);
    }
}
