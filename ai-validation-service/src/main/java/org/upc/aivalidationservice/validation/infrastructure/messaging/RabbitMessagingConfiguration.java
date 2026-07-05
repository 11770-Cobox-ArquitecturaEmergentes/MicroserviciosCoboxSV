package org.upc.aivalidationservice.validation.infrastructure.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RabbitMessagingProperties.class)
public class RabbitMessagingConfiguration {

    @Bean
    TopicExchange coboxEventsExchange(RabbitMessagingProperties properties) {
        return new TopicExchange(properties.exchange(), true, false);
    }

    @Bean
    Queue evidenceUploadConfirmedQueue(RabbitMessagingProperties properties) {
        return QueueBuilder.durable(properties.evidenceUploadConfirmedQueue())
                .deadLetterExchange(properties.exchange())
                .deadLetterRoutingKey(properties.evidenceUploadConfirmedRoutingKey() + ".dlq")
                .build();
    }

    @Bean
    Queue evidenceUploadConfirmedDlq(RabbitMessagingProperties properties) {
        return QueueBuilder.durable(properties.evidenceUploadConfirmedDlq()).build();
    }

    @Bean
    Binding evidenceUploadConfirmedBinding(TopicExchange coboxEventsExchange,
                                           Queue evidenceUploadConfirmedQueue,
                                           RabbitMessagingProperties properties) {
        return BindingBuilder.bind(evidenceUploadConfirmedQueue)
                .to(coboxEventsExchange)
                .with(properties.evidenceUploadConfirmedRoutingKey());
    }

    @Bean
    Binding evidenceUploadConfirmedDlqBinding(TopicExchange coboxEventsExchange,
                                              Queue evidenceUploadConfirmedDlq,
                                              RabbitMessagingProperties properties) {
        return BindingBuilder.bind(evidenceUploadConfirmedDlq)
                .to(coboxEventsExchange)
                .with(properties.evidenceUploadConfirmedRoutingKey() + ".dlq");
    }

    @Bean
    Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory,
                                                                        Jackson2JsonMessageConverter converter) {
        var factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(converter);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}
