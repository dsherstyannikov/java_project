package com.todoapp.backend.services;

import com.todoapp.backend.dto.CreateProjectRequest;
import com.todoapp.backend.dto.ProjectResponse;
import com.todoapp.backend.exceptions.ProjectNotFoundException;
import com.todoapp.backend.exceptions.ResourceAlreadyExistsException;
import com.todoapp.backend.models.Project;
import com.todoapp.backend.models.ProjectMembers;
import com.todoapp.backend.models.ProjectRoles;
import com.todoapp.backend.models.User;
import com.todoapp.backend.repositories.ProjectMembersRepository;
import com.todoapp.backend.repositories.ProjectRepository;
import com.todoapp.backend.repositories.ProjectRolesRepository;
import com.todoapp.backend.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMembersRepository projectMembersRepository;

    @Autowired
    private ProjectRolesRepository projectRolesRepository;

    @Autowired
    private UserRepository userRepository;

    public ProjectResponse createProject(CreateProjectRequest request, Long ownerId) {
        // Проверка, существует ли проект с таким названием у этого пользователя
        Optional<Project> existingProject = projectRepository.findByNameAndOwnerId(request.getName(), ownerId);

        if (existingProject.isPresent()) {
            throw new ResourceAlreadyExistsException("Проект с таким названием уже существует.");
        }

        // Создаем новый проект
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setOwnerId(ownerId); // Устанавливаем владельца
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());

        Project savedProject = projectRepository.save(project);

        // Возвращаем ответ с данными проекта
        return mapToProjectResponse(savedProject);
    }

    public ProjectResponse getProjectById(Long id, Long userId) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + id));

        // Проверяем, является ли пользователь владельцем проекта
        if (!project.getOwnerId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this project");
        }

        return mapToProjectResponse(project);
    }

    public Project getProjectByIdWithOwnerCheck(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));
        
        if (!project.getOwnerId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        
        return project;
    }

    public List<ProjectResponse> getProjectsByOwner(Long ownerId) {
        List<Project> projects = projectRepository.findByOwnerId(ownerId);
        return projects.stream()
                .map(this::mapToProjectResponse)
                .collect(Collectors.toList());
    }

    private ProjectResponse mapToProjectResponse(Project project) {
        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setName(project.getName());
        response.setDescription(project.getDescription());
        response.setOwnerId(project.getOwnerId());
        response.setCreatedAt(project.getCreatedAt());
        response.setUpdatedAt(project.getUpdatedAt());
        return response;
    }

    public ProjectResponse updateProject(Long id, CreateProjectRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + id));

        // Обновляем данные проекта
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setUpdatedAt(LocalDateTime.now());

        Project updatedProject = projectRepository.save(project);
        return mapToProjectResponse(updatedProject);
    }

    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + id));

        projectRepository.delete(project);
    }
}