package com.ahmad.notifyhub.service;

import com.ahmad.notifyhub.config.RabbitMqConfig;
import com.ahmad.notifyhub.messaging.ReminderEmailMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class ReminderEmailPublisher {
    private static final Logger log = LoggerFactory.getLogger(ReminderEmailPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public ReminderEmailPublisher(RabbitTemplate rabbitTemplate){
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitTemplate.setMandatory(true);
        this.rabbitTemplate.setReturnsCallback(returned -> log.error(
                "RabbitMQ could not route reminder email message. Exchange: {}, routing key: {}, reason: {}",
                returned.getExchange(),
                returned.getRoutingKey(),
                returned.getReplyText()
        ));
    }

    public void publish(ReminderEmailMessage message) {
        log.info(
                "Publishing reminder {} to exchange {} with routing key {}",
                message.reminderId(),
                RabbitMqConfig.REMINDER_EXCHANGE,
                RabbitMqConfig.REMINDER_EMAIL_ROUTING_KEY
        );

        rabbitTemplate.convertAndSend(
                RabbitMqConfig.REMINDER_EXCHANGE,
                RabbitMqConfig.REMINDER_EMAIL_ROUTING_KEY,
                message
        );

        log.info("Publish request completed for reminder {}", message.reminderId());
    }
}
