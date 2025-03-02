package com.todoapp.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class LoginRequest {
    private String username;
    
    @NotNull(message = "Email не может быть null.")
    @NotEmpty(message = "Email не может быть пустым.")
    private String email;

    @NotNull(message = "Пароль не может быть null.")
    @NotEmpty(message = "Пароль не может быть пустым.")
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
