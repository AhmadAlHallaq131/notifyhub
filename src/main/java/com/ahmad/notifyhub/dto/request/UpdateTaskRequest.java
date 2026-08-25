package com.ahmad.notifyhub.dto.request;

import com.ahmad.notifyhub.entity.enums.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record UpdateTaskRequest(
        @NotBlank(message = "Task title is required")
        @Size(max = 200, message = "Task title must not exceed 200 characters")
        String title,

        @Size(max = 5000, message = "Task notes must not exceed 5000 characters")
        String notes,

        @NotNull(message = "Task priority is required")
        TaskPriority priority,

        LocalDateTime dueDate,

        @Positive(message = "Task list ID must be positive")
        Long taskListId
) {
}
