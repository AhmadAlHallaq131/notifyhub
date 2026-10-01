package com.ahmad.notifyhub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateSubTaskRequest(
        @NotBlank(message = "Subtask title is required")
        @Size(max = 200, message = "Subtask title must not exceed 200 characters")
        String title
) {
}
