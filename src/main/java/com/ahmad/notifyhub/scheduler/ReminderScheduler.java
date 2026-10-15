package com.ahmad.notifyhub.scheduler;

import com.ahmad.notifyhub.entity.Reminder;
import com.ahmad.notifyhub.messaging.ReminderEmailMessage;
import com.ahmad.notifyhub.repository.ReminderRepository;
import com.ahmad.notifyhub.service.NotificationService;
import com.ahmad.notifyhub.service.ReminderEmailPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReminderScheduler.class);

    private final ReminderRepository reminderRepository;
    private final NotificationService notificationService;
    private final ReminderEmailPublisher reminderEmailPublisher;

    public ReminderScheduler(
            ReminderRepository reminderRepository,
            NotificationService notificationService,
            ReminderEmailPublisher reminderEmailPublisher
    ) {
        this.reminderRepository = reminderRepository;
        this.notificationService = notificationService;
        this.reminderEmailPublisher = reminderEmailPublisher;
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void processDueReminder() {
        LocalDateTime now = LocalDateTime.now();

        List<Reminder> dueReminders =
                reminderRepository.findByActiveTrueAndRemindAtLessThanEqual(now);

        log.info(
                "Reminder scheduler checked at {} and found {} due reminder(s)",
                now,
                dueReminders.size()
        );

        dueReminders.forEach(reminder -> {
            log.info(
                    "Processing reminder {} for task {}",
                    reminder.getId(),
                    reminder.getTask().getId()
            );

            notificationService.createReminderNotification(reminder);

            reminderEmailPublisher.publish(
                    new ReminderEmailMessage(
                            reminder.getId(),
                            reminder.getTask().getUser().getEmail(),
                            reminder.getTask().getTitle()
                    )
            );

            log.info("Published reminder email message for reminder {}", reminder.getId());

            reminder.processTrigger(now);
        });
    }

}
