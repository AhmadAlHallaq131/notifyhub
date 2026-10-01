package com.ahmad.notifyhub.controller;

import com.ahmad.notifyhub.dto.request.CreateSubTaskRequest;
import com.ahmad.notifyhub.dto.request.UpdateSubTaskRequest;
import com.ahmad.notifyhub.dto.response.SubTaskResponse;
import com.ahmad.notifyhub.service.SubTaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SubTaskController {

    private final SubTaskService subTaskService;

    public SubTaskController(SubTaskService subTaskService) {
        this.subTaskService = subTaskService;
    }

    @PostMapping("/tasks/{taskId}/subtasks")
    public ResponseEntity<SubTaskResponse> createSubTask(
            @PathVariable Long taskId,
            @Valid @RequestBody CreateSubTaskRequest request,
            Authentication authentication
    ) {
        SubTaskResponse response = subTaskService.createSubTask(
                taskId,
                request,
                authentication.getName()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/tasks/{taskId}/subtasks")
    public List<SubTaskResponse> getSubTasks(
            @PathVariable Long taskId,
            Authentication authentication
    ) {
        return subTaskService.getSubTasks(taskId, authentication.getName());
    }

    @GetMapping("/subtasks/{subTaskId}")
    public SubTaskResponse getSubTask(
            @PathVariable Long subTaskId,
            Authentication authentication
    ) {
        return subTaskService.getSubTask(subTaskId, authentication.getName());
    }

    @PutMapping("/subtasks/{subTaskId}")
    public SubTaskResponse updateSubTask(
            @PathVariable Long subTaskId,
            @Valid @RequestBody UpdateSubTaskRequest request,
            Authentication authentication
    ) {
        return subTaskService.updateSubTask(subTaskId, request, authentication.getName());
    }

    @PatchMapping("/subtasks/{subTaskId}/complete")
    public SubTaskResponse completeSubTask(
            @PathVariable Long subTaskId,
            Authentication authentication
    ) {
        return subTaskService.completeSubTask(subTaskId, authentication.getName());
    }

    @PatchMapping("/subtasks/{subTaskId}/reopen")
    public SubTaskResponse reopenSubTask(
            @PathVariable Long subTaskId,
            Authentication authentication
    ) {
        return subTaskService.reopenSubTask(subTaskId, authentication.getName());
    }

    @DeleteMapping("/subtasks/{subTaskId}")
    public ResponseEntity<Void> deleteSubTask(
            @PathVariable Long subTaskId,
            Authentication authentication
    ) {
        subTaskService.deleteSubTask(subTaskId, authentication.getName());

        return ResponseEntity.noContent().build();
    }
}
