package com.ahmad.notifyhub.repository;

import com.ahmad.notifyhub.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findByUserEmailOrderByNameAsc(String userEmail);

    Optional<Tag> findByIdAndUserEmail(Long id, String userEmail);

    boolean existsByNameIgnoreCaseAndUserEmail(String name, String userEmail);
}
