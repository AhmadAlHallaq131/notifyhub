package com.ahmad.notifyhub.event;

import com.ahmad.notifyhub.dto.response.NotificationResponse;

public record NotificationCreatedEvent(
        String userEmail,
        NotificationResponse notification
) {
}
