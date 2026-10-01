package com.ahmad.notifyhub.service;

import com.ahmad.notifyhub.dto.request.CreateTaskRequest;
import com.ahmad.notifyhub.dto.request.UpdateTaskRequest;
import com.ahmad.notifyhub.dto.response.TaskResponse;
import com.ahmad.notifyhub.entity.Task;
import com.ahmad.notifyhub.entity.TaskList;
import com.ahmad.notifyhub.entity.Tag;
import com.ahmad.notifyhub.entity.User;
import com.ahmad.notifyhub.entity.enums.TaskPriority;
import com.ahmad.notifyhub.entity.enums.TaskStatus;
import com.ahmad.notifyhub.exception.ResourceNotFoundException;
import com.ahmad.notifyhub.mapper.TaskMapper;
import com.ahmad.notifyhub.repository.TaskListRepository;
import com.ahmad.notifyhub.repository.TaskRepository;
import com.ahmad.notifyhub.repository.TagRepository;
import com.ahmad.notifyhub.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskListRepository taskListRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;

    public TaskService(TaskRepository taskRepository,
                       TaskListRepository taskListRepository,
                       TagRepository tagRepository,
                       UserRepository userRepository,
                       TaskMapper taskMapper
    ) {
        this.taskRepository = taskRepository;
        this.taskListRepository = taskListRepository;
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
        this.taskMapper = taskMapper;
    }

    @Transactional
    public TaskResponse createTask(
            CreateTaskRequest request,
            String userEmail
    ){
        //get the user to get the user data
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found"
                ));

        //find the task list
        TaskList taskList = null;
        if(request.taskListId() != null){
            taskList = taskListRepository
                    .findByIdAndUserEmail(request.taskListId(), userEmail)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Task list not found"
                    ));
        }

        TaskPriority priority = request.priority() == null
                ? TaskPriority.MEDIUM
                : request.priority();

        LocalDateTime now = LocalDateTime.now();

        Task task = new Task(
                request.title().trim(),
                request.notes(),
                priority,
                request.dueDate(),
                user,
                taskList,
                now,
                now
        );
        Task savedTask = taskRepository.save(task);

        return taskMapper.toResponse(savedTask);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasks(String userEmail){
        return taskRepository.findByUserEmailOrderByCreatedAtDesc(userEmail).stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    @Transactional
    public TaskResponse updateTask(Long taskId, UpdateTaskRequest request, String userEmail){
        Task task = taskRepository.findByIdAndUserEmail(taskId, userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Task not found"
                ));

        TaskList taskList = null;

        if (request.taskListId() != null){
            taskList = taskListRepository.findByIdAndUserEmail(request.taskListId(),userEmail)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Task list not found"
                    ));
        }

        task.updateDetails(
                request.title().trim(),
                request.notes(),
                request.priority(),
                request.dueDate(),
                taskList,
                LocalDateTime.now()
        );

        return  taskMapper.toResponse(task);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTask(Long taskId, String userEmail) {
        Task task = taskRepository.findByIdAndUserEmail(taskId, userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        return taskMapper.toResponse(task);
    }

    @Transactional
    public void deleteTask(
            Long taskId,
            String userEmail
    ){
        Task task = taskRepository.findByIdAndUserEmail(taskId,userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Task not found"
                ));
        taskRepository.delete(task);
    }

    @Transactional
    public TaskResponse completeTask(Long taskId, String userEmail){
        Task task = taskRepository.findByIdAndUserEmail(taskId,userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("task not found"));
        LocalDateTime now = LocalDateTime.now();
        task.markCompleted(now,now);
        return taskMapper.toResponse(task);
    }

    @Transactional
    public TaskResponse reopenTask(Long taskId, String userEmail){
        Task task = taskRepository.findByIdAndUserEmail(taskId,userEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException("task not found"));
        task.reopen(LocalDateTime.now());
        return taskMapper.toResponse(task);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getOverdueTasks(String userEmail){
        return taskRepository
                .findByUserEmailAndStatusAndDueDateBeforeOrderByDueDateAsc(
                        userEmail,
                        TaskStatus.PENDING,
                        LocalDateTime.now()
                ).stream()
                .map(taskMapper::toResponse)
                .toList();

    }

    @Transactional
    public TaskResponse addTagToTask(Long taskId, Long tagId, String userEmail) {
        Task task = taskRepository.findByIdAndUserEmail(taskId, userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        Tag tag = tagRepository.findByIdAndUserEmail(tagId, userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));

        task.addTag(tag, LocalDateTime.now());
        return taskMapper.toResponse(task);
    }

    @Transactional
    public TaskResponse removeTagFromTask(Long taskId, Long tagId, String userEmail) {
        Task task = taskRepository.findByIdAndUserEmail(taskId, userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        Tag tag = tagRepository.findByIdAndUserEmail(tagId, userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));

        task.removeTag(tag, LocalDateTime.now());
        return taskMapper.toResponse(task);
    }
}

