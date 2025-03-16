package com.todoapp.backend.controllers;

import com.todoapp.backend.dto.UserProfileResponse;
import com.todoapp.backend.security.JwtUtil;
import com.todoapp.backend.services.UserDetailsImpl;
import com.todoapp.backend.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getUserProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        // Получаем данные пользователя из UserDetailsImpl
        String username = userDetails.getUsername();
        String email = userDetails.getEmail();

        // Создаем ответ
        UserProfileResponse response = new UserProfileResponse(username, email);

        return ResponseEntity.ok(response);
    }
}