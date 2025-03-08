package com.todoapp.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateKanbanGroupRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private Long prevId; // ID предыдущей группы
    private Long nextId; // ID следующей группы

    // Геттеры и сеттеры
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getPrevId() {
        return prevId;
    }

    public void setPrevId(Long prevId) {
        this.prevId = prevId;
    }

    public Long getNextId() {
        return nextId;
    }

    public void setNextId(Long nextId) {
        this.nextId = nextId;
    }
}