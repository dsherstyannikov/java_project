package com.todoapp.backend.services;

import com.todoapp.backend.dto.CreateKanbanGroupRequest;
import com.todoapp.backend.dto.KanbanGroupResponse;
import com.todoapp.backend.exceptions.KanbanGroupNotFoundException;
import com.todoapp.backend.exceptions.ProjectNotFoundException;
import com.todoapp.backend.models.KanbanGroup;
import com.todoapp.backend.models.Project;
import com.todoapp.backend.repositories.KanbanGroupRepository;
import com.todoapp.backend.repositories.ProjectRepository;
import com.todoapp.backend.utils.LexoRank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class KanbanGroupService {

    @Autowired
    private KanbanGroupRepository kanbanGroupRepository;

    @Autowired
    private ProjectRepository projectRepository;

    public KanbanGroupService(KanbanGroupRepository kanbanGroupRepository, ProjectRepository projectRepository) {
        this.kanbanGroupRepository = kanbanGroupRepository;
        this.projectRepository = projectRepository;
    }

    public KanbanGroupResponse createKanbanGroup(Long projectId, CreateKanbanGroupRequest request, Long ownerId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));

        if (!project.getOwnerId().equals(ownerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this project");
        }

        if (request.getPrevId() == null && request.getNextId() == null) {
            List<KanbanGroup> groups = kanbanGroupRepository.findByProjectIdOrderByOrderPositionAsc(projectId);
            KanbanGroup lastGroup = groups.isEmpty() ? null : groups.get(groups.size() - 1);
            String orderPosition = lastGroup == null ? "a0000000"
                    : LexoRank.calculateAfter(lastGroup.getOrderPosition());

            KanbanGroup kanbanGroup = new KanbanGroup();
            kanbanGroup.setName(request.getName());
            kanbanGroup.setOrderPosition(orderPosition);
            kanbanGroup.setProject(project);

            KanbanGroup savedKanbanGroup = kanbanGroupRepository.save(kanbanGroup);
            return mapToKanbanGroupResponse(savedKanbanGroup);
        }

        if ((request.getPrevId() != null && request.getNextId() != null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Either prevId or nextId must be provided, or both null to append at the end");
        }

        KanbanGroup prevGroup = request.getPrevId() != null
                ? kanbanGroupRepository.findById(request.getPrevId()).orElse(null)
                : null;
        KanbanGroup nextGroup = request.getNextId() != null
                ? kanbanGroupRepository.findById(request.getNextId()).orElse(null)
                : null;

        if ((request.getPrevId() != null && prevGroup == null) || (request.getNextId() != null && nextGroup == null)) {
            throw new KanbanGroupNotFoundException("Invalid prevId or nextId provided");
        }

        String orderPosition = calculateOrderPosition(prevGroup, nextGroup);
        if (orderPosition == null) {
            redistributeOrderPositions(projectId);
            orderPosition = calculateOrderPosition(prevGroup, nextGroup);
        }

        KanbanGroup kanbanGroup = new KanbanGroup();
        kanbanGroup.setName(request.getName());
        kanbanGroup.setOrderPosition(orderPosition);
        kanbanGroup.setProject(project);

        KanbanGroup savedKanbanGroup = kanbanGroupRepository.save(kanbanGroup);
        return mapToKanbanGroupResponse(savedKanbanGroup);
    }

    private void redistributeOrderPositions(Long projectId) {
        List<KanbanGroup> groups = kanbanGroupRepository.findByProjectIdOrderByOrderPositionAsc(projectId);
        if (groups.isEmpty()) {
            return;
        }
        String currentPosition = LexoRank.initialRank();
        for (KanbanGroup group : groups) {
            group.setOrderPosition(currentPosition);
            currentPosition = LexoRank.calculateAfter(currentPosition);
        }
        kanbanGroupRepository.saveAll(groups);
    }

    private String calculateOrderPosition(KanbanGroup prevGroup, KanbanGroup nextGroup) {
        if (prevGroup == null && nextGroup == null) {
            return "a0000000";
        } else if (prevGroup == null) {
            return LexoRank.calculateBefore(nextGroup.getOrderPosition());
        } else if (nextGroup == null) {
            return LexoRank.calculateAfter(prevGroup.getOrderPosition());
        } else {
            return LexoRank.calculateBetween(prevGroup.getOrderPosition(), nextGroup.getOrderPosition());
        }
    }

    public KanbanGroupResponse updateKanbanGroup(Long projectId, Long groupId, CreateKanbanGroupRequest request,
            Long ownerId) {
        // Проверяем, существует ли проект и принадлежит ли он пользователю
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));

        if (!project.getOwnerId().equals(ownerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this project");
        }

        // Проверяем, существует ли канбан-группа
        KanbanGroup kanbanGroup = kanbanGroupRepository.findById(groupId)
                .orElseThrow(() -> new KanbanGroupNotFoundException("Kanban group not found with id: " + groupId));

        // Получаем предыдущую и следующую группы
        KanbanGroup prevGroup = request.getPrevId() != null ? kanbanGroupRepository.findById(request.getPrevId())
                .orElseThrow(() -> new KanbanGroupNotFoundException(
                        "Previous group not found with id: " + request.getPrevId()))
                : null;

        KanbanGroup nextGroup = request.getNextId() != null ? kanbanGroupRepository.findById(request.getNextId())
                .orElseThrow(
                        () -> new KanbanGroupNotFoundException("Next group not found with id: " + request.getNextId()))
                : null;

        // Проверка на одновременную передачу prevId и nextId
        // if (request.getPrevId() != null && request.getNextId() != null) {
        //     throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide either prevId or nextId, not both");
        // }

        // String orderPosition = calculateOrderPosition(prevGroup, nextGroup);
        // if (orderPosition == null) {
            // redistributeOrderPositions(projectId);
            // orderPosition = calculateOrderPosition(prevGroup, nextGroup);
        // }

        // Вычисляем новый order_position
        String orderPosition = calculateOrderPosition(prevGroup, nextGroup);

        // Если не удалось вычислить order_position, выполняем перераспределение
        if (orderPosition == null) {
            redistributeOrderPositions(projectId);
            orderPosition = calculateOrderPosition(prevGroup, nextGroup); // Повторно вычисляем после перераспределения
        }

        // Обновляем данные канбан-группы
        if (request.getName() != null){
            kanbanGroup.setName(request.getName());
        }
        kanbanGroup.setOrderPosition(orderPosition);
        // kanbanGroup.setUpdatedAt(LocalDateTime.now());

        KanbanGroup updatedKanbanGroup = kanbanGroupRepository.save(kanbanGroup);
        return mapToKanbanGroupResponse(updatedKanbanGroup);
    }

    

    private KanbanGroupResponse mapToKanbanGroupResponse(KanbanGroup kanbanGroup) {
        KanbanGroupResponse response = new KanbanGroupResponse();
        response.setId(kanbanGroup.getId());
        response.setName(kanbanGroup.getName());
        response.setOrderPosition(kanbanGroup.getOrderPosition());
        response.setProjectId(kanbanGroup.getProject().getId());
        // response.setCreatedAt(kanbanGroup.getCreatedAt());
        // response.setUpdatedAt(kanbanGroup.getUpdatedAt());
        return response;
    }

    public List<KanbanGroupResponse> getKanbanGroupsByProjectId(Long projectId, Long ownerId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));

        if (!project.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("You do not have access to this project");
        }

        return kanbanGroupRepository.findByProjectIdOrderByOrderPositionAsc(projectId)
                .stream()
                .map(this::mapToKanbanGroupResponse)
                .collect(Collectors.toList());
    }

    public void deleteKanbanGroup(Long projectId, Long groupId, Long ownerId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));

        if (!project.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("You do not have access to this project");
        }

        KanbanGroup kanbanGroup = kanbanGroupRepository.findById(groupId)
                .orElseThrow(() -> new KanbanGroupNotFoundException("Kanban group not found with id: " + groupId));

        kanbanGroupRepository.delete(kanbanGroup);
    }
}