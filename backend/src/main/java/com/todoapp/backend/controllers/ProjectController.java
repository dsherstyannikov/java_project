package com.todoapp.backend.controllers;

import com.todoapp.backend.dto.CreateProjectRequest;
import com.todoapp.backend.dto.ProjectResponse;
import com.todoapp.backend.security.JwtUtil;
import com.todoapp.backend.services.ProjectService;
import com.todoapp.backend.repositories.UserRepository; // Подключаем репозиторий
import com.todoapp.backend.models.User; // Подключаем модель User
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository; // Внедряем репозиторий

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @RequestBody CreateProjectRequest request,
            HttpServletRequest httpRequest) {
        // Извлекаем username из токена
        String token = jwtUtil.extractTokenFromRequest(httpRequest);
        String username = jwtUtil.extractUsername(token);

        // Получаем ID пользователя (ownerId) из базы данных
        Long ownerId = getUserIdByUsername(username);

        // Проверка, найден ли пользователь
        if (ownerId == null) {
            return ResponseEntity.status(404).body(null); // Если пользователь не найден
        }

        // Создаем проект
        ProjectResponse response = projectService.createProject(request, ownerId);

        return ResponseEntity.ok(response);
    }

    private Long getUserIdByUsername(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            return userOptional.get().getId(); // Возвращаем ID пользователя
        }
        return null; // Если пользователь не найден
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        // Проверяем токен и извлекаем username
        String token = jwtUtil.extractTokenFromRequest(httpRequest);
        String username = jwtUtil.extractUsername(token);

        // Получаем проект по ID
        ProjectResponse response = projectService.getProjectById(id);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long id,
            @RequestBody CreateProjectRequest request,
            HttpServletRequest httpRequest) {
        // Проверяем токен и извлекаем username
        String token = jwtUtil.extractTokenFromRequest(httpRequest);
        String username = jwtUtil.extractUsername(token);

        // Обновляем проект
        ProjectResponse response = projectService.updateProject(id, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        // Проверяем токен и извлекаем username
        String token = jwtUtil.extractTokenFromRequest(httpRequest);
        String username = jwtUtil.extractUsername(token);

        // Удаляем проект
        projectService.deleteProject(id);

        return ResponseEntity.noContent().build();
    }
}
