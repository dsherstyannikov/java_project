package com.todoapp.backend.repositories;

import com.todoapp.backend.models.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByKanbanGroupIdOrderByOrderPositionAsc(Long groupId);
    List<Task> findByKanbanGroupId(Long groupId);
    List<Task> findByKanbanGroupIdAndIsCompletedOrderByOrderPositionAsc(Long groupId, boolean isCompleted);
}