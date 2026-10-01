package com.ahmad.notifyhub.dto.request;

import com.ahmad.notifyhub.entity.enums.ReminderRepeatFrequency;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateReminderRequest(
        @NotNull(message = "Reminder time is required")
        @Future(message = "Reminder time must be in the future")
        LocalDateTime remindAt,

        ReminderRepeatFrequency repeatFrequency
) {
}
