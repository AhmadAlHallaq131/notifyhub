package com.ahmad.notifyhub.controller;

import com.ahmad.notifyhub.dto.request.CreateTaskRequest;
import com.ahmad.notifyhub.dto.request.UpdateTaskRequest;
import com.ahmad.notifyhub.dto.response.PageResponse;
import com.ahmad.notifyhub.dto.response.TaskResponse;
import com.ahmad.notifyhub.entity.enums.TaskPriority;
import com.ahmad.notifyhub.entity.enums.TaskStatus;
import com.ahmad.notifyhub.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@Validated
@Tag(name = "Tasks", description = "Create, organize, and track tasks owned by the authenticated user.")
@SecurityRequirement(name = "bearerAuth")
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

    @GetMapping("/search")
    public List<TaskResponse> searchTasks(
            @RequestParam String q,
            Authentication authentication
    ) {
        return taskService.searchTasks(q, authentication.getName());
    }

    @GetMapping("/page")
    @Operation(summary = "List tasks with pagination, sorting, and filters")
    public PageResponse<TaskResponse> getTaskPage(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) Long taskListId,
            @RequestParam(required = false) String q,
            Authentication authentication
    ) {
        return taskService.getTaskPage(
                authentication.getName(), page, size, sortBy, direction, status, priority, taskListId, q
        );
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

    @GetMapping("/my-day")
    public List<TaskResponse> getMyDayTasks(Authentication authentication) {
        return taskService.getMyDayTasks(authentication.getName());
    }

    @PatchMapping("/{taskId}/my-day")
    public TaskResponse addTaskToMyDay(
            @PathVariable Long taskId,
            Authentication authentication
    ) {
        return taskService.addTaskToMyDay(taskId, authentication.getName());
    }

    @DeleteMapping("/{taskId}/my-day")
    public TaskResponse removeTaskFromMyDay(
            @PathVariable Long taskId,
            Authentication authentication
    ) {
        return taskService.removeTaskFromMyDay(taskId, authentication.getName());
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

