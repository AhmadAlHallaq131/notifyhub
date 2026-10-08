package com.ahmad.notifyhub.controller;

import com.ahmad.notifyhub.dto.response.NotificationResponse;
import com.ahmad.notifyhub.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationResponse> getNotifications(Authentication authentication) {
        return notificationService.getNotifications(authentication.getName());
    }

    @GetMapping("/unread")
    public List<NotificationResponse> getUnreadNotifications(Authentication authentication) {
        return notificationService.getUnreadNotifications(authentication.getName());
    }

    @GetMapping("/{notificationId}")
    public NotificationResponse getNotification(
            @PathVariable Long notificationId,
            Authentication authentication
    ) {
        return notificationService.getNotification(notificationId, authentication.getName());
    }

    @PatchMapping("/{notificationId}/read")
    public NotificationResponse markNotificationRead(
            @PathVariable Long notificationId,
            Authentication authentication
    ) {
        return notificationService.markNotificationRead(notificationId, authentication.getName());
    }

    @PatchMapping("/read-all")
    public List<NotificationResponse> markAllNotificationsRead(Authentication authentication) {
        return notificationService.markAllNotificationsRead(authentication.getName());
    }
}
