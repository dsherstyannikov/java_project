package com.todoapp.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @NotNull(message = "Имя пользователя не может быть null.")
    @NotEmpty(message = "Имя пользователя не может быть пустым.")
    @Size(min = 3, max = 20, message = "Имя пользователя должно содержать от 3 до 20 символов.")
    private String username;

    @NotNull(message = "Email не может быть null.")
    @NotEmpty(message = "Email не может быть пустым.")
    @Email(message = "Неверный формат email.")
    private String email;

    @NotNull(message = "Пароль не может быть null.")
    @NotEmpty(message = "Пароль не может быть пустым.")
    @Size(min = 6, message = "Пароль должен содержать минимум 6 символов.")
    private String password;

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
