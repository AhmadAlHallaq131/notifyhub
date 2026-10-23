package com.ahmad.notifyhub.service;

import com.ahmad.notifyhub.dto.response.NotificationResponse;
import com.ahmad.notifyhub.entity.Notification;
import com.ahmad.notifyhub.entity.Reminder;
import com.ahmad.notifyhub.entity.Task;
import com.ahmad.notifyhub.entity.enums.NotificationType;
import com.ahmad.notifyhub.event.NotificationCreatedEvent;
import com.ahmad.notifyhub.exception.ResourceNotFoundException;
import com.ahmad.notifyhub.mapper.NotificationMapper;
import com.ahmad.notifyhub.repository.NotificationRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final ApplicationEventPublisher eventPublisher;

    public NotificationService(
            NotificationRepository notificationRepository,
            NotificationMapper notificationMapper,
            ApplicationEventPublisher eventPublisher
    ) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(String userEmail) {
        return notificationRepository.findByUserEmailOrderByCreatedAtDesc(userEmail)
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(String userEmail) {
        return notificationRepository.findByUserEmailAndReadAtIsNullOrderByCreatedAtDesc(userEmail)
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public NotificationResponse getNotification(Long notificationId, String userEmail) {
        return notificationMapper.toResponse(getOwnedNotification(notificationId, userEmail));
    }

    @Transactional
    public NotificationResponse markNotificationRead(Long notificationId, String userEmail) {
        Notification notification = getOwnedNotification(notificationId, userEmail);
        notification.markRead(LocalDateTime.now());

        return notificationMapper.toResponse(notification);
    }

    @Transactional
    public List<NotificationResponse> markAllNotificationsRead(String userEmail) {
        LocalDateTime now = LocalDateTime.now();
        List<Notification> unreadNotifications = notificationRepository
                .findByUserEmailAndReadAtIsNullOrderByCreatedAtDesc(userEmail);

        unreadNotifications.forEach(notification -> notification.markRead(now));

        return unreadNotifications.stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Transactional
    public void createReminderNotification(Reminder reminder) {
        Task task = reminder.getTask();
        Notification notification = new Notification(
                task.getUser(),
                NotificationType.REMINDER,
                "Reminder: " + task.getTitle(),
                "Your reminder is due.",
                LocalDateTime.now()
        );

        Notification savedNotification = notificationRepository.save(notification);
        NotificationResponse notificationResponse = notificationMapper.toResponse(savedNotification);

        eventPublisher.publishEvent(
                new NotificationCreatedEvent(task.getUser().getEmail(), notificationResponse)
        );
    }

    private Notification getOwnedNotification(Long notificationId, String userEmail) {
        return notificationRepository.findByIdAndUserEmail(notificationId, userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
    }
}
