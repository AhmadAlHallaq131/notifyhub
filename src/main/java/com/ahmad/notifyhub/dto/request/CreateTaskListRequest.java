package com.ahmad.notifyhub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTaskListRequest (
        @NotBlank(message = "List name is required")
        @Size(max = 100, message = "List name must not exceed 100 characters")
        String name
){
}
