package org.upc.aivalidationservice.validation.interfaces.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.upc.aivalidationservice.validation.application.internal.commandservices.AiValidationCommandService;

@Component
public class EvidenceUploadConfirmedListener {

    private final AiValidationCommandService commandService;
    private final ObjectMapper objectMapper;

    public EvidenceUploadConfirmedListener(AiValidationCommandService commandService, ObjectMapper objectMapper) {
        this.commandService = commandService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "${cobox.rabbitmq.evidence-upload-confirmed-queue:ai.evidence-upload-confirmed}")
    public void onEvidenceUploadConfirmed(String payload) throws JsonProcessingException {
        var event = objectMapper.readValue(payload, EvidenceUploadConfirmedEvent.class);
        commandService.handleEvidenceUploadConfirmed(event);
    }
}
