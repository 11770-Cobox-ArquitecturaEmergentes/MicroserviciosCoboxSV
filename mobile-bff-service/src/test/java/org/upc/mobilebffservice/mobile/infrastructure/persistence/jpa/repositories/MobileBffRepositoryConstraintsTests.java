package org.upc.mobilebffservice.mobile.infrastructure.persistence.jpa.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.upc.mobilebffservice.mobile.application.internal.commandservices.UploadIntentCommandServiceImpl;
import org.upc.mobilebffservice.mobile.domain.model.aggregates.UploadIntent;
import org.upc.mobilebffservice.mobile.domain.model.entities.OutboxMessage;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class MobileBffRepositoryConstraintsTests {

    @Autowired
    private UploadIntentRepository uploadIntentRepository;

    @Autowired
    private OutboxMessageRepository outboxMessageRepository;

    @Test
    void uploadIntentClientEvidenceIdIsUnique() {
        var clientEvidenceId = UUID.randomUUID();
        uploadIntentRepository.saveAndFlush(uploadIntent(clientEvidenceId));

        assertThatThrownBy(() -> uploadIntentRepository.saveAndFlush(uploadIntent(clientEvidenceId)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void outboxEventTypeAndAggregateIdAreUnique() {
        var aggregateId = UUID.randomUUID().toString();
        outboxMessageRepository.saveAndFlush(outbox(aggregateId));

        assertThatThrownBy(() -> outboxMessageRepository.saveAndFlush(outbox(aggregateId)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private UploadIntent uploadIntent(UUID clientEvidenceId) {
        return new UploadIntent(
                clientEvidenceId,
                7L,
                100L,
                20L,
                "DELIVERY_PHOTO",
                "drivers/7/routes/20/orders/100/evidences/" + clientEvidenceId,
                "b6d81b360a5672d80c27430f39153e2c6f32f2255f6a071d9f8efb9bd2c7d1c2",
                "image/jpeg",
                2048L,
                Instant.now().plusSeconds(900)
        );
    }

    private OutboxMessage outbox(String aggregateId) {
        return new OutboxMessage(
                UploadIntentCommandServiceImpl.EVIDENCE_UPLOAD_CONFIRMED,
                aggregateId,
                "{}",
                Instant.now()
        );
    }
}
