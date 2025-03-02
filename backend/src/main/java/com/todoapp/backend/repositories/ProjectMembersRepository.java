package com.todoapp.backend.repositories;

import com.todoapp.backend.models.ProjectMembers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectMembersRepository extends JpaRepository<ProjectMembers, Long> {

    // Поиск участника по проекту и пользователю
    Optional<ProjectMembers> findByProjectIdAndUserId(Long projectId, Long userId);
}
