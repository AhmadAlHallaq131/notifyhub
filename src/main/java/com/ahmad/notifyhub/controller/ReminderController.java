package com.ahmad.notifyhub.controller;

import com.ahmad.notifyhub.dto.request.CreateReminderRequest;
import com.ahmad.notifyhub.dto.request.UpdateReminderRequest;
import com.ahmad.notifyhub.dto.response.ReminderResponse;
import com.ahmad.notifyhub.service.ReminderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @PostMapping("/tasks/{taskId}/reminders")
    public ResponseEntity<ReminderResponse> createReminder(
            @PathVariable("taskId") Long taskId,
            @Valid @RequestBody CreateReminderRequest request,
            Authentication authentication
    ) {
        ReminderResponse response = reminderService.createReminder(
                taskId,
                request,
                authentication.getName()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/tasks/{taskId}/reminders")
    public List<ReminderResponse> getTaskReminders(
            @PathVariable Long taskId,
            Authentication authentication
    ) {
        return reminderService.getTaskReminders(taskId, authentication.getName());
    }

    @GetMapping("/reminders/{reminderId}")
    public ReminderResponse getReminder(
            @PathVariable Long reminderId,
            Authentication authentication
    ) {
        return reminderService.getReminder(reminderId, authentication.getName());
    }

    @PutMapping("/reminders/{reminderId}")
    public ReminderResponse updateReminder(
            @PathVariable Long reminderId,
            @Valid @RequestBody UpdateReminderRequest request,
            Authentication authentication
    ) {
        return reminderService.updateReminder(reminderId, request, authentication.getName());
    }

    @PatchMapping("/reminders/{reminderId}/activate")
    public ReminderResponse activateReminder(
            @PathVariable Long reminderId,
            Authentication authentication
    ) {
        return reminderService.activateReminder(reminderId, authentication.getName());
    }

    @PatchMapping("/reminders/{reminderId}/deactivate")
    public ReminderResponse deactivateReminder(
            @PathVariable Long reminderId,
            Authentication authentication
    ) {
        return reminderService.deactivateReminder(reminderId, authentication.getName());
    }

    @DeleteMapping("/reminders/{reminderId}")
    public ResponseEntity<Void> deleteReminder(
            @PathVariable Long reminderId,
            Authentication authentication
    ) {
        reminderService.deleteReminder(reminderId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
