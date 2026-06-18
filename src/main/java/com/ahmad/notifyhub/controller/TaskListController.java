package com.ahmad.notifyhub.controller;

import com.ahmad.notifyhub.dto.request.CreateTaskListRequest;
import com.ahmad.notifyhub.dto.response.TaskListResponse;
import com.ahmad.notifyhub.service.TaskListService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lists")
public class TaskListController {

    private final TaskListService taskListService;

    public TaskListController(TaskListService taskListService) {
        this.taskListService = taskListService;
    }

    @PostMapping
    public ResponseEntity<TaskListResponse> createList(
            @Valid @RequestBody CreateTaskListRequest request,
            Authentication authentication
            ){
        TaskListResponse taskListResponse = taskListService.createList(
                request,
                authentication.getName()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(taskListResponse);
    }

    @GetMapping
    public ResponseEntity<List<TaskListResponse>> getLists(Authentication authentication){
        List<TaskListResponse> responses = taskListService.getLists(authentication.getName());
        return ResponseEntity.ok(responses);
    }


}
