package com.ahmad.notifyhub.entity;

import com.ahmad.notifyhub.entity.enums.TaskPriority;
import com.ahmad.notifyhub.entity.enums.TaskStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false , length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TaskPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TaskStatus status;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;


    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_list_id")
    private TaskList taskList;


    @Column(name = "created_at",nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Task() {
    }

    public Task(
                String title,
                String notes,
                TaskPriority priority,
                LocalDateTime dueDate,
                User user,
                TaskList taskList,
                LocalDateTime createdAt,
                LocalDateTime updatedAt
    ) {
        this.title = title;
        this.notes = notes;
        this.priority = priority;
        this.status = TaskStatus.PENDING;
        this.dueDate = dueDate;
        this.user = user;
        this.taskList = taskList;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void updateDetails(
            String title,
            String notes,
            TaskPriority priority,
            LocalDateTime dueDate,
            TaskList taskList,
            LocalDateTime updatedAt
    ) {
        this.title = title;
        this.notes = notes;
        this.priority = priority;
        this.dueDate = dueDate;
        this.taskList = taskList;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getNotes() {
        return notes;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public User getUser() {
        return user;
    }

    public TaskList getTaskList() {
        return taskList;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
