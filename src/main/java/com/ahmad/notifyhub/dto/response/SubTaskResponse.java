package com.ahmad.notifyhub.dto.response;

import java.time.LocalDateTime;

public record SubTaskResponse(
        Long id,
        String title,
        boolean completed,
        LocalDateTime completedAt,
        Long taskId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
