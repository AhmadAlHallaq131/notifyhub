package com.ahmad.notifyhub.controller;

import com.ahmad.notifyhub.dto.request.CreateTaskRequest;
import com.ahmad.notifyhub.dto.request.UpdateTaskRequest;
import com.ahmad.notifyhub.dto.response.TaskResponse;
import com.ahmad.notifyhub.entity.Task;
import com.ahmad.notifyhub.mapper.TaskMapper;
import com.ahmad.notifyhub.repository.TaskRepository;
import com.ahmad.notifyhub.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final TaskRepository taskRepository;

    public TaskController(TaskService taskService, TaskRepository taskRepository) {
        this.taskService = taskService;
        this.taskRepository = taskRepository;
    }

    @PostMapping()
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody CreateTaskRequest request,
            Authentication authentication
    ){
        TaskResponse taskResponse = taskService.createTask(
                request,
                authentication.getName()
                );
        return ResponseEntity.status(HttpStatus.CREATED).body(taskResponse);
    }

    @GetMapping()
    public ResponseEntity<List<TaskResponse>> getTasks(
            Authentication authentication
    ){
        return ResponseEntity.ok(
                taskService.getTasks(authentication.getName()));
    }

    @GetMapping("/{taskId}")
    public  TaskResponse getTask(
            @PathVariable Long taskId,
            Authentication authentication
    ){
        return taskService.getTask(taskId, authentication.getName());
    }

    @PutMapping("/{taskId}")
    public TaskResponse updateTask(
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskRequest request,
            Authentication authentication
            ){
        return taskService
                .updateTask(taskId,request,authentication.getName());
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long taskId,
            Authentication authentication
    ){
        taskService.deleteTask(taskId, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{taskId}/complete")
    public TaskResponse completeTask(
            @PathVariable Long taskId,
            Authentication authentication
    ){
        return taskService
                .completeTask(taskId , authentication.getName());
    }

    @PatchMapping("/{taskId}/reopen")
    public TaskResponse reopenTask(
            @PathVariable Long taskId,
            Authentication authentication
    ){
        return taskService
                .reopenTask(taskId, authentication.getName());
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<TaskResponse>> getOverdueTasks(
            Authentication authentication
    ){
        List<TaskResponse> tasks = taskService.getOverdueTasks(authentication.getName());
        return ResponseEntity.ok(tasks);
    }

    @PostMapping("/{taskId}/tags/{tagId}")
    public TaskResponse addTagToTask(
            @PathVariable Long taskId,
            @PathVariable Long tagId,
            Authentication authentication
    ) {
        return taskService.addTagToTask(taskId, tagId, authentication.getName());
    }

    @DeleteMapping("/{taskId}/tags/{tagId}")
    public TaskResponse removeTagFromTask(
            @PathVariable Long taskId,
            @PathVariable Long tagId,
            Authentication authentication
    ) {
        return taskService.removeTagFromTask(taskId, tagId, authentication.getName());
    }
}

