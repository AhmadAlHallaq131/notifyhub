package com.ahmad.notifyhub.service;

import com.ahmad.notifyhub.entity.Reminder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {
    private final JavaMailSender mailSender;
    private final String from;

    public EmailNotificationService(
            JavaMailSender mailSender,
            @Value("${app.mail.from}") String from
    ){
        this.mailSender = mailSender;
        this.from = from;
    }

    public void sendReminderEmail(Reminder reminder){
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(from);
        message.setTo(reminder.getTask().getUser().getEmail());
        message.setSubject("Reminder: " + reminder.getTask().getTitle());
        message.setText("Your reminder is due for the task: " + reminder.getTask().getTitle());

        mailSender.send(message);
    }
}
