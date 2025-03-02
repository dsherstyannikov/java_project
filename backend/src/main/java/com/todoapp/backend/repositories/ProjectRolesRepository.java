package com.todoapp.backend.repositories;

import com.todoapp.backend.models.ProjectRoles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface ProjectRolesRepository extends JpaRepository<ProjectRoles, Long> {

        // Отключить кэширование, если это необходимо
        @Transactional(readOnly = true)
        public Optional<ProjectRoles> findByName(String name);
        
}
