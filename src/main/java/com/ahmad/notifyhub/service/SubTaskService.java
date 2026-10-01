package com.ahmad.notifyhub.service;

import com.ahmad.notifyhub.dto.request.CreateSubTaskRequest;
import com.ahmad.notifyhub.dto.request.UpdateSubTaskRequest;
import com.ahmad.notifyhub.dto.response.SubTaskResponse;
import com.ahmad.notifyhub.entity.SubTask;
import com.ahmad.notifyhub.entity.Task;
import com.ahmad.notifyhub.exception.ResourceNotFoundException;
import com.ahmad.notifyhub.mapper.SubTaskMapper;
import com.ahmad.notifyhub.repository.SubTaskRepository;
import com.ahmad.notifyhub.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SubTaskService {

    private final SubTaskRepository subTaskRepository;
    private final TaskRepository taskRepository;
    private final SubTaskMapper subTaskMapper;

    public SubTaskService(
            SubTaskRepository subTaskRepository,
            TaskRepository taskRepository,
            SubTaskMapper subTaskMapper
    ) {
        this.subTaskRepository = subTaskRepository;
        this.taskRepository = taskRepository;
        this.subTaskMapper = subTaskMapper;
    }

    @Transactional
    public SubTaskResponse createSubTask(
            Long taskId,
            CreateSubTaskRequest request,
            String userEmail
    ) {
        Task task = getOwnedTask(taskId, userEmail);
        LocalDateTime now = LocalDateTime.now();

        SubTask subTask = new SubTask(request.title().trim(), task, now, now);
        SubTask savedSubTask = subTaskRepository.save(subTask);

        return subTaskMapper.toResponse(savedSubTask);
    }

    @Transactional(readOnly = true)
    public List<SubTaskResponse> getSubTasks(Long taskId, String userEmail) {
        getOwnedTask(taskId, userEmail);

        return subTaskRepository.findByTaskIdAndTaskUserEmailOrderByCreatedAtAsc(taskId, userEmail)
                .stream()
                .map(subTaskMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SubTaskResponse getSubTask(Long subTaskId, String userEmail) {
        return subTaskMapper.toResponse(getOwnedSubTask(subTaskId, userEmail));
    }

    @Transactional
    public SubTaskResponse updateSubTask(
            Long subTaskId,
            UpdateSubTaskRequest request,
            String userEmail
    ) {
        SubTask subTask = getOwnedSubTask(subTaskId, userEmail);
        subTask.updateTitle(request.title().trim(), LocalDateTime.now());

        return subTaskMapper.toResponse(subTask);
    }

    @Transactional
    public SubTaskResponse completeSubTask(Long subTaskId, String userEmail) {
        SubTask subTask = getOwnedSubTask(subTaskId, userEmail);
        LocalDateTime now = LocalDateTime.now();
        subTask.markCompleted(now, now);

        return subTaskMapper.toResponse(subTask);
    }

    @Transactional
    public SubTaskResponse reopenSubTask(Long subTaskId, String userEmail) {
        SubTask subTask = getOwnedSubTask(subTaskId, userEmail);
        subTask.reopen(LocalDateTime.now());

        return subTaskMapper.toResponse(subTask);
    }

    @Transactional
    public void deleteSubTask(Long subTaskId, String userEmail) {
        subTaskRepository.delete(getOwnedSubTask(subTaskId, userEmail));
    }

    private Task getOwnedTask(Long taskId, String userEmail) {
        return taskRepository.findByIdAndUserEmail(taskId, userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    private SubTask getOwnedSubTask(Long subTaskId, String userEmail) {
        return subTaskRepository.findByIdAndTaskUserEmail(subTaskId, userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Subtask not found"));
    }
}
