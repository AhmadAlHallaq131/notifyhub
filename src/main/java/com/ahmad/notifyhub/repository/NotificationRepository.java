package com.ahmad.notifyhub.repository;

import com.ahmad.notifyhub.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserEmailOrderByCreatedAtDesc(String userEmail);

    List<Notification> findByUserEmailAndReadAtIsNullOrderByCreatedAtDesc(String userEmail);

    Optional<Notification> findByIdAndUserEmail(Long id, String userEmail);
}
