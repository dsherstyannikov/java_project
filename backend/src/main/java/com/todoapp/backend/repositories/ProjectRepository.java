package com.todoapp.backend.repositories;

import com.todoapp.backend.models.Project;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByNameAndOwnerId(String name, Long ownerId);

    List<Project> findByOwnerId(Long ownerId);

    @Query("SELECT p FROM Project p JOIN ProjectMembers pm ON p.id = pm.project.id WHERE pm.user.id = :userId")
    List<Project> findByMemberId(@Param("userId") Long userId);

    @Query("SELECT p FROM Project p WHERE p.ownerId = :userId OR p.id IN (SELECT pm.project.id FROM ProjectMembers pm WHERE pm.user.id = :userId)")
    List<Project> findByOwnerIdOrMemberId(@Param("userId") Long userId);
}
