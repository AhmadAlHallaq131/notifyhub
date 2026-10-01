package com.ahmad.notifyhub.repository;

import com.ahmad.notifyhub.entity.SubTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubTaskRepository extends JpaRepository<SubTask, Long> {

    List<SubTask> findByTaskIdAndTaskUserEmailOrderByCreatedAtAsc(Long taskId, String userEmail);

    Optional<SubTask> findByIdAndTaskUserEmail(Long id, String userEmail);
}
