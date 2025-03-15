package com.todoapp.backend.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;

public class CreateTaskRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    private Long prevId; // ID предыдущей задачи
    private Long nextId; // ID следующей задачи
    private Long kanbanGroupId;
    private LocalDateTime dueDate;
    private Integer priority;
    private Long colorId;

    // Геттеры и сеттеры
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Long getColorId() {
        return colorId;
    }

    public void setColorId(Long colorId) {
        this.colorId = colorId;
    }

    public Long getKanbanGroupId() {
        return kanbanGroupId;
    }

    public void setKanbanGroup(Long kanbanGroupId){
        this.kanbanGroupId = kanbanGroupId;
    }
}