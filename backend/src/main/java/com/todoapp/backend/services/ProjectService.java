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
import java.util.Optional;

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
            // throw new ResponseStatusException(HttpStatus.CONFLICT, "Проект с таким названием уже существует");
        }
    
        // Создаем новый проект
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setOwnerId(ownerId); // Устанавливаем владельца
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
    
        Project savedProject = projectRepository.save(project);
    
        // Find the user by ownerId (instead of userId)
        User user = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    
        // Получаем роль владельца (Owner)
        // System.out.println(projectRolesRepository.findByName("Owner"));
        ProjectRoles role = projectRolesRepository.findByName("Owner")
                .orElseThrow(() -> new RuntimeException("Role not found"));
    
        // Создаем новый ProjectMember
        ProjectMembers projectMember = new ProjectMembers();
        projectMember.setProject(project); // Set Project
        projectMember.setUser(user); // Set User
        projectMember.setRoleInProject(role); // Set Role
        projectMember.setJoinedAt(LocalDateTime.now()); // Set Join Time
    
        // Сохраняем ProjectMember в репозитории
        projectMembersRepository.save(projectMember);
    
        // Возвращаем ответ с данными проекта
        return mapToProjectResponse(savedProject);
    }

    public ProjectResponse getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + id));
        return mapToProjectResponse(project);
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