package com.todoapp.backend.dto;

public class JwtResponse {
    private String accessToken;

    public JwtResponse(String accessToken) {
        this.accessToken = accessToken;
    }

    // Геттеры
    public String getAccessToken() {
        return accessToken;
    }
}