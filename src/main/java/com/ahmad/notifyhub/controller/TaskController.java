package com.ahmad.notifyhub.controller;

import com.ahmad.notifyhub.dto.request.CreateTaskRequest;
import com.ahmad.notifyhub.dto.request.UpdateTaskRequest;
import com.ahmad.notifyhub.dto.response.TaskResponse;
import com.ahmad.notifyhub.mapper.TaskMapper;
import com.ahmad.notifyhub.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
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

    @PutMapping("/{taskId}")
    public TaskResponse updateTask(
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskRequest request,
            Authentication authentication
            ){
        return taskService
                .updateTask(taskId,request,authentication.getName());
    }
}

