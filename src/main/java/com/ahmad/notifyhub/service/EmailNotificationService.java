package com.ahmad.notifyhub.service;

import com.ahmad.notifyhub.entity.Reminder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {
    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    private final JavaMailSender mailSender;
    private final String from;

    public EmailNotificationService(
            JavaMailSender mailSender,
            @Value("${app.mail.from}") String from
    ){
        this.mailSender = mailSender;
        this.from = from;
    }

    public void sendReminderEmail(Reminder reminder) {
        sendReminderEmail(
                reminder.getTask().getUser().getEmail(),
                reminder.getTask().getTitle()
        );
    }

    public void sendReminderEmail(String recipientEmail, String taskTitle) {
        log.info("Sending reminder email for task '{}'", taskTitle);

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(from);
        message.setTo(recipientEmail);
        message.setSubject("Reminder: " + taskTitle);
        message.setText("Your reminder is due for the task: " + taskTitle);

        mailSender.send(message);

        log.info("Reminder email sent for task '{}'", taskTitle);
    }
}
