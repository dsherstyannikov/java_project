package com.todoapp.backend.repositories;

import com.todoapp.backend.models.KanbanGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KanbanGroupRepository extends JpaRepository<KanbanGroup, Long> {
    List<KanbanGroup> findByProjectIdOrderByOrderPositionAsc(Long projectId);
    List<KanbanGroup> findByProjectId(Long projectId);
}