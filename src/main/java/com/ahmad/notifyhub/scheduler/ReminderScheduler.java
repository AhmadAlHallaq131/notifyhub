package com.ahmad.notifyhub.scheduler;

import com.ahmad.notifyhub.repository.ReminderRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class ReminderScheduler {

    private final ReminderRepository reminderRepository;

    public ReminderScheduler(ReminderRepository reminderRepository) {
        this.reminderRepository = reminderRepository;
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void processDueReminder(){
        LocalDateTime now = LocalDateTime.now();

        reminderRepository.findByActiveTrueAndRemindAtLessThanEqual(now)
                .forEach(reminder -> reminder.processTrigger(now));
    }

}
