package com.todoapp.backend.dto;

public class UserProfileResponse {
    private String username;
    private String email;

    // Конструкторы, геттеры и сеттеры
    public UserProfileResponse() {}

    public UserProfileResponse(String username, String email) {
        this.username = username;
        this.email = email;
    }

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
}