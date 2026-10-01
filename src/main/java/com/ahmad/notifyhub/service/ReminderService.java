package com.ahmad.notifyhub.service;

import com.ahmad.notifyhub.dto.request.CreateReminderRequest;
import com.ahmad.notifyhub.dto.request.UpdateReminderRequest;
import com.ahmad.notifyhub.dto.response.ReminderResponse;
import com.ahmad.notifyhub.entity.Reminder;
import com.ahmad.notifyhub.entity.Task;
import com.ahmad.notifyhub.entity.enums.ReminderRepeatFrequency;
import com.ahmad.notifyhub.exception.ResourceNotFoundException;
import com.ahmad.notifyhub.mapper.ReminderMapper;
import com.ahmad.notifyhub.repository.ReminderRepository;
import com.ahmad.notifyhub.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final TaskRepository taskRepository;
    private final ReminderMapper reminderMapper;

    public ReminderService(
            ReminderRepository reminderRepository,
            TaskRepository taskRepository,
            ReminderMapper reminderMapper
    ) {
        this.reminderRepository = reminderRepository;
        this.taskRepository = taskRepository;
        this.reminderMapper = reminderMapper;
    }

    @Transactional
    public ReminderResponse createReminder(
            Long taskId,
            CreateReminderRequest request,
            String userEmail
    ) {
        Task task = getOwnedTask(taskId, userEmail);
        LocalDateTime now = LocalDateTime.now();
        ReminderRepeatFrequency repeatFrequency = request.repeatFrequency() == null
                ? ReminderRepeatFrequency.NONE
                : request.repeatFrequency();

        Reminder reminder = new Reminder(
                request.remindAt(),
                repeatFrequency,
                task,
                now,
                now
        );

        return reminderMapper.toResponse(reminderRepository.save(reminder));
    }

    @Transactional(readOnly = true)
    public List<ReminderResponse> getTaskReminders(Long taskId, String userEmail) {
        getOwnedTask(taskId, userEmail);

        return reminderRepository.findByTaskIdAndTaskUserEmailOrderByRemindAtAsc(taskId, userEmail)
                .stream()
                .map(reminderMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReminderResponse getReminder(Long reminderId, String userEmail) {
        return reminderMapper.toResponse(getOwnedReminder(reminderId, userEmail));
    }

    @Transactional
    public ReminderResponse updateReminder(
            Long reminderId,
            UpdateReminderRequest request,
            String userEmail
    ) {
        Reminder reminder = getOwnedReminder(reminderId, userEmail);
        reminder.updateDetails(
                request.remindAt(),
                request.repeatFrequency(),
                LocalDateTime.now()
        );

        return reminderMapper.toResponse(reminder);
    }

    @Transactional
    public ReminderResponse activateReminder(Long reminderId, String userEmail) {
        Reminder reminder = getOwnedReminder(reminderId, userEmail);
        reminder.activate(LocalDateTime.now());

        return reminderMapper.toResponse(reminder);
    }

    @Transactional
    public ReminderResponse deactivateReminder(Long reminderId, String userEmail) {
        Reminder reminder = getOwnedReminder(reminderId, userEmail);
        reminder.deactivate(LocalDateTime.now());

        return reminderMapper.toResponse(reminder);
    }

    @Transactional
    public void deleteReminder(Long reminderId, String userEmail) {
        reminderRepository.delete(getOwnedReminder(reminderId, userEmail));
    }

    private Task getOwnedTask(Long taskId, String userEmail) {
        return taskRepository.findByIdAndUserEmail(taskId, userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    private Reminder getOwnedReminder(Long reminderId, String userEmail) {
        return reminderRepository.findByIdAndTaskUserEmail(reminderId, userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found"));
    }
}
