package com.ahmad.notifyhub.repository;

import com.ahmad.notifyhub.entity.Task;
import com.ahmad.notifyhub.entity.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task,Long> {

    List<Task> findByUserEmailOrderByCreatedAtDesc(String email);

    Optional<Task> findByIdAndUserEmail(Long id, String email);

    List<Task> findByTaskListIdAndUserEmail(Long taskListId, String userEmail);

    List<Task> findByUserEmailAndMyDayDateOrderByCreatedAtAsc(String userEmail, LocalDate myDayDate);

    List<Task> findByUserEmailAndStatusAndDueDateBeforeOrderByDueDateAsc(
            String email,
            TaskStatus status,
            LocalDateTime dueDate
    );

}
