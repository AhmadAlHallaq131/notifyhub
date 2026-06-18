package com.ahmad.notifyhub.service;

import com.ahmad.notifyhub.dto.request.CreateTaskListRequest;
import com.ahmad.notifyhub.dto.response.TaskListResponse;
import com.ahmad.notifyhub.entity.TaskList;
import com.ahmad.notifyhub.entity.User;
import com.ahmad.notifyhub.mapper.TaskListMapper;
import com.ahmad.notifyhub.repository.TaskListRepository;
import com.ahmad.notifyhub.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskListService {

    private final TaskListRepository taskListRepository;
    private final UserRepository userRepository;
    private final TaskListMapper taskListMapper;

    public TaskListService(TaskListRepository taskListRepository,
                           UserRepository userRepository,
                           TaskListMapper taskListMapper) {
        this.taskListRepository = taskListRepository;
        this.userRepository = userRepository;
        this.taskListMapper = taskListMapper;
    }

    @Transactional
    public TaskListResponse createList(CreateTaskListRequest request, String email
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not Found"));

        LocalDateTime now = LocalDateTime.now();

        TaskList taskList = new TaskList(
                request.name().trim(),
                user,
                now,
                now
        );

        TaskList taskListSave = taskListRepository.save(taskList);

        return taskListMapper.toResponse(taskListSave);
    }


    @Transactional(readOnly = true)
    public List<TaskListResponse> getLists(String email) {
        return taskListRepository.findByUserEmailOrderByCreatedAtAsc(email)
                .stream()
                .map(taskListMapper::toResponse)
                .toList();
    }

}


