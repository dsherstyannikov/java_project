package com.todoapp.backend.controllers;

import com.todoapp.backend.dto.CreateTaskRequest;
import com.todoapp.backend.dto.TaskResponse;
import com.todoapp.backend.services.TaskService;
import com.todoapp.backend.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/groups/{groupId}/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable Long groupId,
            @RequestBody CreateTaskRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        TaskResponse response = taskService.createTask(groupId, request, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getTasksByGroupId(
            @PathVariable Long groupId,
            @RequestParam(required = false) Boolean completed,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<TaskResponse> responses = taskService.getTasksByGroupId(groupId, userDetails.getId(), completed);
        return ResponseEntity.ok(responses);
    }


    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long groupId,
            @PathVariable Long taskId,
            @RequestBody CreateTaskRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        TaskResponse response = taskService.updateTask(groupId, taskId, request, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long groupId,
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        taskService.deleteTask(groupId, taskId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}