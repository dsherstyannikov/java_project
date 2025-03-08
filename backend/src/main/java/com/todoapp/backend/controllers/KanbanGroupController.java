package com.todoapp.backend.controllers;

import com.todoapp.backend.dto.CreateKanbanGroupRequest;
import com.todoapp.backend.dto.KanbanGroupResponse;
import com.todoapp.backend.exceptions.ErrorDetails;
import com.todoapp.backend.services.KanbanGroupService;
import com.todoapp.backend.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/groups")
public class KanbanGroupController {

    @Autowired
    private KanbanGroupService kanbanGroupService;

    public KanbanGroupController(KanbanGroupService kanbanGroupService) {
        this.kanbanGroupService = kanbanGroupService;
    }

    @PostMapping
    public ResponseEntity<?> createKanbanGroup(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateKanbanGroupRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        // Проверка на ошибки валидации
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(new ErrorDetails(new Date(), "Invalid request body", bindingResult.toString()));
        }

        KanbanGroupResponse response = kanbanGroupService.createKanbanGroup(projectId, request, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<KanbanGroupResponse>> getKanbanGroupsByProjectId(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<KanbanGroupResponse> responses = kanbanGroupService.getKanbanGroupsByProjectId(projectId, userDetails.getId());
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<?> updateKanbanGroup(
            @PathVariable Long projectId,
            @PathVariable Long groupId,
            @Valid @RequestBody CreateKanbanGroupRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        // Проверка на ошибки валидации
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(new ErrorDetails(new Date(), "Invalid request body", bindingResult.toString()));
        }

        KanbanGroupResponse response = kanbanGroupService.updateKanbanGroup(projectId, groupId, request, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteKanbanGroup(
            @PathVariable Long projectId,
            @PathVariable Long groupId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        kanbanGroupService.deleteKanbanGroup(projectId, groupId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}