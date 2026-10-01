package com.ahmad.notifyhub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "subtasks")
public class SubTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false)
    private boolean completed;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Task task;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected SubTask() {
    }

    public SubTask(String title, Task task, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.title = title;
        this.completed = false;
        this.task = task;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void updateTitle(String title, LocalDateTime updatedAt) {
        this.title = title;
        this.updatedAt = updatedAt;
    }

    public void markCompleted(LocalDateTime completedAt, LocalDateTime updatedAt) {
        this.completed = true;
        this.completedAt = completedAt;
        this.updatedAt = updatedAt;
    }

    public void reopen(LocalDateTime updatedAt) {
        this.completed = false;
        this.completedAt = null;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public boolean isCompleted() {
        return completed;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public Task getTask() {
        return task;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
