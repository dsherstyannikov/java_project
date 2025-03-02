package com.todoapp.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    private String username;
    private String email;
    private String password;

    // Геттер для username
    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    // Геттер для password
    public String getPassword() {
        return password;
    }

    // Сеттеры и другие методы, если необходимо
}
