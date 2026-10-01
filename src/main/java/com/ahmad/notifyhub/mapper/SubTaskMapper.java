package com.ahmad.notifyhub.mapper;

import com.ahmad.notifyhub.dto.response.SubTaskResponse;
import com.ahmad.notifyhub.entity.SubTask;
import org.springframework.stereotype.Component;

@Component
public class SubTaskMapper {

    public SubTaskResponse toResponse(SubTask subTask) {
        return new SubTaskResponse(
                subTask.getId(),
                subTask.getTitle(),
                subTask.isCompleted(),
                subTask.getCompletedAt(),
                subTask.getTask().getId(),
                subTask.getCreatedAt(),
                subTask.getUpdatedAt()
        );
    }
}
