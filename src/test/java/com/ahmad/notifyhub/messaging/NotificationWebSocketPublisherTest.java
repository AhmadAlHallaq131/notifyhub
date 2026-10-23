package com.ahmad.notifyhub.messaging;

import com.ahmad.notifyhub.dto.response.NotificationResponse;
import com.ahmad.notifyhub.entity.enums.NotificationType;
import com.ahmad.notifyhub.event.NotificationCreatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationWebSocketPublisherTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Test
    void publishesNotificationToTheOwningUser() {
        NotificationResponse notification = new NotificationResponse(
                5L, NotificationType.REMINDER, "Reminder", "Your task is due.", false, null, LocalDateTime.now()
        );
        NotificationWebSocketPublisher publisher = new NotificationWebSocketPublisher(messagingTemplate);

        publisher.publish(new NotificationCreatedEvent("user@example.com", notification));

        verify(messagingTemplate).convertAndSendToUser(
                "user@example.com", NotificationWebSocketPublisher.NOTIFICATION_DESTINATION, notification
        );
    }
}
