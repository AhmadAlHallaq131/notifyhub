package com.ahmad.notifyhub.dto.response;

import com.ahmad.notifyhub.entity.enums.TaskPriority;
import com.ahmad.notifyhub.entity.enums.TaskStatus;

import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        String title,
        String notes,
        TaskPriority priority,
        TaskStatus status,
        LocalDateTime dueDate,
        LocalDateTime completedAt,
        Long taskListId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}
