package com.todoapp.backend.repositories;

import com.todoapp.backend.models.Project;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByNameAndOwnerId(String name, Long ownerId);
}