package com.todoapp.backend.controllers;

import com.todoapp.backend.dto.CreateProjectRequest;
import com.todoapp.backend.dto.ProjectResponse;
import com.todoapp.backend.dto.TaskResponse;
import com.todoapp.backend.security.JwtUtil;
import com.todoapp.backend.services.ProjectService;
import com.todoapp.backend.services.TaskService;
import com.todoapp.backend.services.UserDetailsImpl;
import com.todoapp.backend.repositories.UserRepository; // Подключаем репозиторий
import com.todoapp.backend.models.Project;
import com.todoapp.backend.models.User; // Подключаем модель User
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TaskService taskService;

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
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        ProjectResponse project = projectService.getProjectById(id, userDetails.getId());
        return ResponseEntity.ok(project);
    }

    @GetMapping("/")
    public ResponseEntity<List<ProjectResponse>> getProjectsByOwner(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<ProjectResponse> projects = projectService.getProjectsByOwner(userDetails.getId());
        return ResponseEntity.ok(projects);
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

    @GetMapping("/{id}/tasks")
    public ResponseEntity<Map<Long, List<TaskResponse>>> getTasksByProjectId(
            @PathVariable Long id,
            @RequestParam(required = false) Boolean completed,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        // Проверяем доступ к проекту и получаем его
        Project project = projectService.getProjectByIdWithOwnerCheck(id, userDetails.getId());

        // Получаем задачи проекта, сгруппированные по группам
        Map<Long, List<TaskResponse>> groupedTasks = taskService.getTasksByProjectId(project, completed);

        return ResponseEntity.ok(groupedTasks);
    }
}
