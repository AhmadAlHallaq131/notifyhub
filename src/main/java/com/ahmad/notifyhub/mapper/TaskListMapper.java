package com.ahmad.notifyhub.mapper;

import com.ahmad.notifyhub.dto.response.TaskListResponse;
import com.ahmad.notifyhub.entity.TaskList;
import org.springframework.stereotype.Component;

@Component
public class TaskListMapper {

    public TaskListResponse toResponse(TaskList taskList) {
        return new TaskListResponse(
                taskList.getId(),
                taskList.getName(),
                taskList.getCreatedAt(),
                taskList.getUpdatedAt()
        );

    }
}
