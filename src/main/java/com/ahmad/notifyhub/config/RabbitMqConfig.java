package com.ahmad.notifyhub.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    public static final String REMINDER_EXCHANGE = "notifyhub.reminder.exchange";
    public static final String REMINDER_EMAIL_QUEUE = "notifyhub.reminder.email.queue";
    public static final String REMINDER_EMAIL_ROUTING_KEY = "reminder.email";
    public static final String REMINDER_EMAIL_DLX = "notifyhub.reminder.email.dlx";
    public static final String REMINDER_EMAIL_DLQ = "notifyhub.reminder.email.dlq";
    public static final String REMINDER_EMAIL_DLQ_ROUTING_KEY = "reminder.email.dead";

    @Bean
    public DirectExchange reminderExchange(){
        return new DirectExchange(REMINDER_EXCHANGE);
    }

    @Bean
    public Queue reminderEmailQueue() {
        return QueueBuilder
                .durable(REMINDER_EMAIL_QUEUE)
                .deadLetterExchange(REMINDER_EMAIL_DLX)
                .deadLetterRoutingKey(REMINDER_EMAIL_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding reminderEmailBinding(
            Queue reminderEmailQueue,
            DirectExchange reminderExchange
    ) {
        return BindingBuilder
                .bind(reminderEmailQueue)
                .to(reminderExchange)
                .with(REMINDER_EMAIL_ROUTING_KEY);
    }

    @Bean
    public DirectExchange reminderEmailDeadLetterExchange() {
        return new DirectExchange(REMINDER_EMAIL_DLX);
    }

    @Bean
    public Queue reminderEmailDeadLetterQueue() {
        return QueueBuilder.durable(REMINDER_EMAIL_DLQ).build();
    }

    @Bean
    public Binding reminderEmailDeadLetterBinding(
            Queue reminderEmailDeadLetterQueue,
            DirectExchange reminderEmailDeadLetterExchange
    ) {
        return BindingBuilder
                .bind(reminderEmailDeadLetterQueue)
                .to(reminderEmailDeadLetterExchange)
                .with(REMINDER_EMAIL_DLQ_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
