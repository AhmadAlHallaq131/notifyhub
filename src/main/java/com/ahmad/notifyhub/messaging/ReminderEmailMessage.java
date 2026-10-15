package com.ahmad.notifyhub.messaging;

public record ReminderEmailMessage(
        Long reminderId,
        String recipientEmail,
        String taskTitle
) {
}
