package com.ahmad.notifyhub.dto.response;

import java.time.LocalDateTime;

public record TagResponse(
        Long id,
        String name,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
