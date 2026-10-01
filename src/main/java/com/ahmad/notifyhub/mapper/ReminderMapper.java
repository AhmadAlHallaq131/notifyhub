package com.ahmad.notifyhub.mapper;

import com.ahmad.notifyhub.dto.response.ReminderResponse;
import com.ahmad.notifyhub.entity.Reminder;
import org.springframework.stereotype.Component;

@Component
public class ReminderMapper {

    public ReminderResponse toResponse(Reminder reminder) {
        return new ReminderResponse(
                reminder.getId(),
                reminder.getTask().getId(),
                reminder.getRemindAt(),
                reminder.getRepeatFrequency(),
                reminder.isActive(),
                reminder.getCreatedAt(),
                reminder.getUpdatedAt()
        );
    }
}
