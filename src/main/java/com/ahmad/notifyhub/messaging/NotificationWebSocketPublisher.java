package com.ahmad.notifyhub.messaging;

import com.ahmad.notifyhub.event.NotificationCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class NotificationWebSocketPublisher {

    public static final String NOTIFICATION_DESTINATION = "/queue/notifications";

    private static final Logger log = LoggerFactory.getLogger(NotificationWebSocketPublisher.class);

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationWebSocketPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(NotificationCreatedEvent event) {
        messagingTemplate.convertAndSendToUser(
                event.userEmail(),
                NOTIFICATION_DESTINATION,
                event.notification()
        );

        log.info("Published live notification {} to {}", event.notification().id(), event.userEmail());
    }
}
