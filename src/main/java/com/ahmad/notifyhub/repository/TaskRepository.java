package com.ahmad.notifyhub.repository;

import com.ahmad.notifyhub.entity.Task;
import com.ahmad.notifyhub.entity.enums.TaskPriority;
import com.ahmad.notifyhub.entity.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("""
            select task from Task task
            where task.user.email = :userEmail
              and (
                    lower(task.title) like lower(concat('%', :query, '%'))
                    or lower(coalesce(task.notes, '')) like lower(concat('%', :query, '%'))
              )
            order by task.createdAt desc
            """)
    List<Task> searchByUserEmail(
            @Param("userEmail") String userEmail,
            @Param("query") String query
    );

    @Query("""
            select task from Task task
            where task.user.email = :userEmail
              and (:status is null or task.status = :status)
              and (:priority is null or task.priority = :priority)
              and (:taskListId is null or task.taskList.id = :taskListId)
              and (
                    :query is null
                    or lower(task.title) like lower(concat('%', :query, '%'))
                    or lower(coalesce(task.notes, '')) like lower(concat('%', :query, '%'))
              )
            """)
    Page<Task> findPageByFilters(
            @Param("userEmail") String userEmail,
            @Param("status") TaskStatus status,
            @Param("priority") TaskPriority priority,
            @Param("taskListId") Long taskListId,
            @Param("query") String query,
            Pageable pageable
    );

}
