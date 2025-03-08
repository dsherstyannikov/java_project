package com.todoapp.backend.exceptions;

public class KanbanGroupNotFoundException extends RuntimeException {
    public KanbanGroupNotFoundException(String message) {
        super(message);
    }
}