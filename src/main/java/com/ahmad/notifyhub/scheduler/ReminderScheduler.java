package com.ahmad.notifyhub.scheduler;

import com.ahmad.notifyhub.repository.ReminderRepository;
import com.ahmad.notifyhub.service.EmailNotificationService;
import com.ahmad.notifyhub.service.NotificationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class ReminderScheduler {

    private final ReminderRepository reminderRepository;
    private final NotificationService notificationService;
    private final EmailNotificationService emailNotificationService;

    public ReminderScheduler(
            ReminderRepository reminderRepository,
            NotificationService notificationService,
            EmailNotificationService emailNotificationService
    ) {
        this.reminderRepository = reminderRepository;
        this.notificationService = notificationService;
        this.emailNotificationService = emailNotificationService;
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void processDueReminder(){
        LocalDateTime now = LocalDateTime.now();

        reminderRepository.findByActiveTrueAndRemindAtLessThanEqual(now)
                .forEach(reminder -> {
                    notificationService.createReminderNotification(reminder);
                    emailNotificationService.sendReminderEmail(reminder);
                    reminder.processTrigger(now);
                });
    }

}
