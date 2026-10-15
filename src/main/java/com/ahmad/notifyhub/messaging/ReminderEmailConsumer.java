package com.ahmad.notifyhub.messaging;

import com.ahmad.notifyhub.config.RabbitMqConfig;
import com.ahmad.notifyhub.service.EmailNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ReminderEmailConsumer {

    private static final Logger log = LoggerFactory.getLogger(ReminderEmailConsumer.class);

    private final EmailNotificationService emailNotificationService;

    public  ReminderEmailConsumer(EmailNotificationService emailNotificationService){
        this.emailNotificationService  = emailNotificationService;
    }

    @RabbitListener(queues = RabbitMqConfig.REMINDER_EMAIL_QUEUE)
    public  void consumer(ReminderEmailMessage message){
        log.info("Consumer received reminder email message for reminder {}", message.reminderId());

        try {
            emailNotificationService.sendReminderEmail(
                    message.recipientEmail(),
                    message.taskTitle()
            );
            log.info("Consumer completed reminder email message for reminder {}", message.reminderId());
        } catch (RuntimeException exception) {
            log.error(
                    "Consumer failed to process reminder email message for reminder {}",
                    message.reminderId(),
                    exception
            );
            throw exception;
        }
    }
}
