package com.ahmad.notifyhub.entity;

import com.ahmad.notifyhub.entity.enums.ReminderRepeatFrequency;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "reminders")
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "remind_at", nullable = false)
    private LocalDateTime remindAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "repeat_frequency", nullable = false, length = 10)
    private ReminderRepeatFrequency repeatFrequency;

    @Column(nullable = false)
    private boolean active;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Task task;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Reminder() {
    }

    public Reminder(
            LocalDateTime remindAt,
            ReminderRepeatFrequency repeatFrequency,
            Task task,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.remindAt = remindAt;
        this.repeatFrequency = repeatFrequency;
        this.active = true;
        this.task = task;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void updateDetails(
            LocalDateTime remindAt,
            ReminderRepeatFrequency repeatFrequency,
            LocalDateTime updatedAt
    ) {
        this.remindAt = remindAt;
        this.repeatFrequency = repeatFrequency;
        this.updatedAt = updatedAt;
    }

    public void activate(LocalDateTime updatedAt) {
        this.active = true;
        this.updatedAt = updatedAt;
    }

    public void deactivate(LocalDateTime updatedAt) {
        this.active = false;
        this.updatedAt = updatedAt;
    }

    public void processTrigger(LocalDateTime now) {
        if (repeatFrequency == ReminderRepeatFrequency.NONE) {
            deactivate(now);
            return;
        }

        LocalDateTime nextRemindAt = remindAt;

        do {
            nextRemindAt = switch (repeatFrequency) {
                case DAILY -> nextRemindAt.plusDays(1);
                case WEEKLY -> nextRemindAt.plusDays(7);
                case MONTHLY -> nextRemindAt.plusMonths(1);
                case NONE -> throw new IllegalStateException("One-time reminders cannot repeat");
            };
        } while (!nextRemindAt.isAfter(now));

        this.remindAt = nextRemindAt;
        this.updatedAt = now;
    }

}
