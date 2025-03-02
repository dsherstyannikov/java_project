package com.todoapp.backend.repositories;

import com.todoapp.backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    // Add these methods
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    
}
