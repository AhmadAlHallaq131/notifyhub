package com.ahmad.notifyhub.repository;

import com.ahmad.notifyhub.entity.TaskList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskListRepository extends JpaRepository<TaskList,Long> {

    List<TaskList> findByUserEmailOrderByCreatedAtAsc(String email);

    Optional<TaskList> findByIdAndUserEmail(Long id, String email);

    boolean existsByIdAndUserEmail(Long id, String email);


}
