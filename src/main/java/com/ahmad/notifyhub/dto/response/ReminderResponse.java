package com.ahmad.notifyhub.dto.response;

import com.ahmad.notifyhub.entity.enums.ReminderRepeatFrequency;

import java.time.LocalDateTime;

public record ReminderResponse(
        Long id,
        Long taskId,
        LocalDateTime remindAt,
        ReminderRepeatFrequency repeatFrequency,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
