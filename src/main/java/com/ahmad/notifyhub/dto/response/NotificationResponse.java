package com.ahmad.notifyhub.dto.response;

import com.ahmad.notifyhub.entity.enums.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        NotificationType type,
        String title,
        String message,
        boolean read,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {
}
