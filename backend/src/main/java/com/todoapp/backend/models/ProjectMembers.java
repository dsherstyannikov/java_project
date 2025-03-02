package com.todoapp.backend.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ProjectMembers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "role_in_project", nullable = false)
    private ProjectRoles roleInProject;

    private LocalDateTime joinedAt;

    // Constructors, getters, and setters

    public ProjectMembers() {
    }

    public ProjectMembers(Project project, User user, ProjectRoles roleInProject) {
        this.project = project;
        this.user = user;
        this.roleInProject = roleInProject;
        this.joinedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ProjectRoles getRoleInProject() {
        return roleInProject;
    }

    public void setRoleInProject(ProjectRoles roleInProject) {
        this.roleInProject = roleInProject;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}
