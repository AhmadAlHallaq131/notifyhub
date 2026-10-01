package com.ahmad.notifyhub.entity;

import com.ahmad.notifyhub.entity.enums.TaskPriority;
import com.ahmad.notifyhub.entity.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
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

    @ManyToMany
    @JoinTable(
            name = "task_tags",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();


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

    public void markCompleted(
            LocalDateTime completedAt,
            LocalDateTime updatedAt
    ) {
        this.status = TaskStatus.COMPLETED;
        this.completedAt = completedAt;
        this.updatedAt = updatedAt;
    }

    public void reopen(
            LocalDateTime updatedAt
    ){
        this.status = TaskStatus.PENDING;
        this.completedAt = null;
        this.updatedAt = updatedAt;
    }

    public void removeFromTaskList(LocalDateTime updatedAt) {
        this.taskList = null;
        this.updatedAt = updatedAt;
    }

    public void addTag(Tag tag, LocalDateTime updatedAt) {
        this.tags.add(tag);
        this.updatedAt = updatedAt;
    }

    public void removeTag(Tag tag, LocalDateTime updatedAt) {
        this.tags.remove(tag);
        this.updatedAt = updatedAt;
    }

}
