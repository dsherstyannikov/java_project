package com.todoapp.backend.repositories;

import com.todoapp.backend.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    /**
     * Находит роль по её имени.
     *
     * @param name Имя роли (например, "ROLE_USER").
     * @return Optional, содержащий роль, если она найдена.
     */
    Optional<Role> findByName(String name);
}