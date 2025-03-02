package com.todoapp.backend.dto;

public class RefreshTokenRequest {
    private String refreshToken;

    // Геттеры и сеттеры
    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}