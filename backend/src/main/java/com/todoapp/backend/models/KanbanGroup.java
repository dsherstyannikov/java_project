package com.todoapp.backend.models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "kanban_groups")
public class KanbanGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String orderPosition;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // @CreationTimestamp
    // @Column(name = "created_at", nullable = false, updatable = false)
    // private LocalDateTime createdAt;

    // @UpdateTimestamp
    // @Column(name = "updated_at")
    // private LocalDateTime updatedAt;

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrderPosition() {
        return orderPosition;
    }

    public void setOrderPosition(String orderPosition) {
        this.orderPosition = orderPosition;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    // public LocalDateTime getCreatedAt() {
    //     return createdAt;
    // }

    // public void setCreatedAt(LocalDateTime createdAt) {
    //     this.createdAt = createdAt;
    // }

    // public LocalDateTime getUpdatedAt() {
    //     return updatedAt;
    // }

    // public void setUpdatedAt(LocalDateTime updatedAt) {
    //     this.updatedAt = updatedAt;
    // }
}