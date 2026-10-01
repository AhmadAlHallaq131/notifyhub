package com.ahmad.notifyhub.mapper;

import com.ahmad.notifyhub.dto.response.TaskResponse;
import com.ahmad.notifyhub.entity.Task;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TaskMapper {
    public TaskResponse toResponse(Task task){
        Long taskListId = task.getTaskList() == null
                ? null
                : task.getTaskList().getId();

        List<Long> tagIds = task.getTags().stream()
                .map(tag -> tag.getId())
                .sorted()
                .toList();

        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getNotes(),
                task.getPriority(),
                task.getStatus(),
                task.getDueDate(),
                task.getCompletedAt(),
                task.getMyDayDate(),
                taskListId,
                task.getCreatedAt(),
                task.getUpdatedAt(),
                tagIds
        );
    }
}
