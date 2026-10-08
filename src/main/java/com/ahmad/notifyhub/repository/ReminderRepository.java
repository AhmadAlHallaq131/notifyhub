package com.ahmad.notifyhub.repository;

import com.ahmad.notifyhub.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    List<Reminder> findByTaskIdAndTaskUserEmailOrderByRemindAtAsc(Long taskId, String userEmail);

    Optional<Reminder> findByIdAndTaskUserEmail(Long id, String userEmail);

    List<Reminder> findByActiveTrueAndRemindAtLessThanEqual(LocalDateTime time);
}