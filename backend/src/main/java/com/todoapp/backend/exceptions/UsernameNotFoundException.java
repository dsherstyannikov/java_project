package com.todoapp.backend.exceptions;

public class UsernameNotFoundException extends RuntimeException {

    // Конструктор с сообщением об ошибке
    public UsernameNotFoundException(String message) {
        super(message);
    }

    // Конструктор с сообщением об ошибке и причиной
    public UsernameNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}