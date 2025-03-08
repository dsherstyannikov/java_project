package com.todoapp.backend.repositories;

import com.todoapp.backend.models.Color;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ColorRepository extends JpaRepository<Color, Long> {
}