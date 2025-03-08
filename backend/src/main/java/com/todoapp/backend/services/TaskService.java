package com.todoapp.backend.services;

import com.todoapp.backend.dto.CreateTaskRequest;
import com.todoapp.backend.dto.TaskResponse;
import com.todoapp.backend.exceptions.KanbanGroupNotFoundException;
import com.todoapp.backend.exceptions.TaskNotFoundException;
import com.todoapp.backend.models.*;
import com.todoapp.backend.services.NotificationService;
import com.todoapp.backend.repositories.*;
import com.todoapp.backend.utils.LexoRank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// import javax.management.Notification;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private KanbanGroupRepository kanbanGroupRepository;

    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ColorRepository colorRepository;

    @Autowired
    private NotificationService notificationService;

    public List<TaskResponse> getTasksByGroupId(Long groupId, Long userId) {
        KanbanGroup kanbanGroup = kanbanGroupRepository.findById(groupId)
                .orElseThrow(() -> new KanbanGroupNotFoundException("Kanban group not found with id: " + groupId));

        if (!kanbanGroup.getProject().getOwnerId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this group");
        }

        List<Task> tasks = taskRepository.findByKanbanGroupIdOrderByOrderPositionAsc(groupId);
        return tasks.stream().map(this::mapToTaskResponse).collect(Collectors.toList());
    }

    public TaskResponse createTask(Long groupId, CreateTaskRequest request, Long authorId) {
        // Проверяем группу и права доступа
        KanbanGroup kanbanGroup = kanbanGroupRepository.findById(groupId)
                .orElseThrow(() -> new KanbanGroupNotFoundException("Kanban group not found with id: " + groupId));

        if (!kanbanGroup.getProject().getOwnerId().equals(authorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this group");
        }

        // Получаем автора и цвет (если указан)
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Color color = request.getColorId() != null ? colorRepository.findById(request.getColorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Color not found")) : null;

        // Расчёт order_position
        String orderPosition;
        if (request.getPrevId() == null && request.getNextId() == null) {
            // Если оба не переданы – добавляем в конец задач группы
            List<Task> tasks = taskRepository.findByKanbanGroupIdOrderByOrderPositionAsc(groupId);
            Task lastTask = tasks.isEmpty() ? null : tasks.get(tasks.size() - 1);
            orderPosition = lastTask == null ? LexoRank.initialRank()
                    : LexoRank.calculateAfter(lastTask.getOrderPosition());
        } else if (request.getPrevId() != null && request.getNextId() != null) {
            // Если вставка между двумя задачами
            Task prevTask = taskRepository.findById(request.getPrevId())
                    .orElseThrow(
                            () -> new TaskNotFoundException("Previous task not found with id: " + request.getPrevId()));
            Task nextTask = taskRepository.findById(request.getNextId())
                    .orElseThrow(
                            () -> new TaskNotFoundException("Next task not found with id: " + request.getNextId()));
            orderPosition = LexoRank.calculateBetween(prevTask.getOrderPosition(), nextTask.getOrderPosition());
            if (orderPosition == null) {
                redistributeTaskOrderPositions(groupId);
                orderPosition = LexoRank.calculateBetween(prevTask.getOrderPosition(), nextTask.getOrderPosition());
            }
        } else if (request.getPrevId() == null && request.getNextId() != null) {
            // Вставка в начало – next передан, prev отсутствует
            Task nextTask = taskRepository.findById(request.getNextId())
                    .orElseThrow(
                            () -> new TaskNotFoundException("Next task not found with id: " + request.getNextId()));
            orderPosition = LexoRank.calculateBefore(nextTask.getOrderPosition());
        } else if (request.getPrevId() != null && request.getNextId() == null) {
            // Вставка в конец – prev передан, next отсутствует
            Task prevTask = taskRepository.findById(request.getPrevId())
                    .orElseThrow(
                            () -> new TaskNotFoundException("Previous task not found with id: " + request.getPrevId()));
            orderPosition = LexoRank.calculateAfter(prevTask.getOrderPosition());
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid combination of prevId and nextId");
        }

        // Создаём задачу
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setOrderPosition(orderPosition);
        task.setKanbanGroup(kanbanGroup);
        task.setAuthor(author);
        task.setDueDate(request.getDueDate());
        task.setPriority(request.getPriority());
        task.setColor(color);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        Task savedTask = taskRepository.save(task);

        // Если указано время окончания, создаём уведомление
        if (task.getDueDate() != null) {
            String message = "Напоминание: задача \"" + task.getTitle() + "\" заканчивается через час!";
            notificationService.createNotificationForTask(task.getAuthor(), message, task.getDueDate());
        }

        return mapToTaskResponse(savedTask);
    }

    public TaskResponse updateTask(Long groupId, Long taskId, CreateTaskRequest request, Long userId) {
        // Проверяем группу и права доступа
        KanbanGroup kanbanGroup = kanbanGroupRepository.findById(groupId)
                .orElseThrow(() -> new KanbanGroupNotFoundException("Kanban group not found with id: " + groupId));

        if (!kanbanGroup.getProject().getOwnerId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this group");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        // Обновляем данные задачи
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        task.setPriority(request.getPriority());
        task.setColor(request.getColorId() != null ? colorRepository.findById(request.getColorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Color not found")) : null);
        task.setUpdatedAt(LocalDateTime.now());

        // Если переданы параметры для перестановки, пересчитываем order_position
        if (request.getPrevId() != null || request.getNextId() != null) {
            String orderPosition;
            if (request.getPrevId() != null && request.getNextId() != null) {
                Task prevTask = taskRepository.findById(request.getPrevId())
                        .orElseThrow(() -> new TaskNotFoundException(
                                "Previous task not found with id: " + request.getPrevId()));
                Task nextTask = taskRepository.findById(request.getNextId())
                        .orElseThrow(
                                () -> new TaskNotFoundException("Next task not found with id: " + request.getNextId()));
                orderPosition = LexoRank.calculateBetween(prevTask.getOrderPosition(), nextTask.getOrderPosition());
                if (orderPosition == null) {
                    redistributeTaskOrderPositions(groupId);
                    orderPosition = LexoRank.calculateBetween(prevTask.getOrderPosition(), nextTask.getOrderPosition());
                }
            } else if (request.getPrevId() == null && request.getNextId() != null) {
                Task nextTask = taskRepository.findById(request.getNextId())
                        .orElseThrow(
                                () -> new TaskNotFoundException("Next task not found with id: " + request.getNextId()));
                orderPosition = LexoRank.calculateBefore(nextTask.getOrderPosition());
            } else if (request.getPrevId() != null && request.getNextId() == null) {
                Task prevTask = taskRepository.findById(request.getPrevId())
                        .orElseThrow(() -> new TaskNotFoundException(
                                "Previous task not found with id: " + request.getPrevId()));
                orderPosition = LexoRank.calculateAfter(prevTask.getOrderPosition());
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid combination of prevId and nextId");
            }
            task.setOrderPosition(orderPosition);
        }

        // Если dueDate изменено, обновляем уведомление
        if (task.getDueDate() != null) {
            String message = "Напоминание: задача \"" + task.getTitle() + "\" заканчивается через час!";
            notificationService.createNotificationForTask(task.getAuthor(), message, task.getDueDate());
        }

        Task updatedTask = taskRepository.save(task);
        return mapToTaskResponse(updatedTask);
    }

    // public TaskResponse updateTask(Long groupId, Long taskId, CreateTaskRequest request, Long userId) {
    //     // Проверяем группу и права доступа
    //     KanbanGroup kanbanGroup = kanbanGroupRepository.findById(groupId)
    //             .orElseThrow(() -> new KanbanGroupNotFoundException("Kanban group not found with id: " + groupId));

    //     if (!kanbanGroup.getProject().getOwnerId().equals(userId)) {
    //         throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this group");
    //     }

    //     Task task = taskRepository.findById(taskId)
    //             .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

    //     // Обновляем данные задачи
    //     task.setTitle(request.getTitle());
    //     task.setDescription(request.getDescription());
    //     task.setDueDate(request.getDueDate());
    //     task.setPriority(request.getPriority());
    //     task.setColor(request.getColorId() != null ? colorRepository.findById(request.getColorId())
    //             .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Color not found")) : null);
    //     task.setUpdatedAt(LocalDateTime.now());

    //     // Если переданы параметры для перестановки, пересчитываем order_position
    //     if (request.getPrevId() != null || request.getNextId() != null) {
    //         String orderPosition;
    //         if (request.getPrevId() != null && request.getNextId() != null) {
    //             Task prevTask = taskRepository.findById(request.getPrevId())
    //                     .orElseThrow(() -> new TaskNotFoundException(
    //                             "Previous task not found with id: " + request.getPrevId()));
    //             Task nextTask = taskRepository.findById(request.getNextId())
    //                     .orElseThrow(
    //                             () -> new TaskNotFoundException("Next task not found with id: " + request.getNextId()));
    //             orderPosition = LexoRank.calculateBetween(prevTask.getOrderPosition(), nextTask.getOrderPosition());
    //             if (orderPosition == null) {
    //                 redistributeTaskOrderPositions(groupId);
    //                 orderPosition = LexoRank.calculateBetween(prevTask.getOrderPosition(), nextTask.getOrderPosition());
    //             }
    //         } else if (request.getPrevId() == null && request.getNextId() != null) {
    //             Task nextTask = taskRepository.findById(request.getNextId())
    //                     .orElseThrow(
    //                             () -> new TaskNotFoundException("Next task not found with id: " + request.getNextId()));
    //             orderPosition = LexoRank.calculateBefore(nextTask.getOrderPosition());
    //         } else if (request.getPrevId() != null && request.getNextId() == null) {
    //             Task prevTask = taskRepository.findById(request.getPrevId())
    //                     .orElseThrow(() -> new TaskNotFoundException(
    //                             "Previous task not found with id: " + request.getPrevId()));
    //             orderPosition = LexoRank.calculateAfter(prevTask.getOrderPosition());
    //         } else {
    //             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid combination of prevId and nextId");
    //         }
    //         task.setOrderPosition(orderPosition);
    //     }

    //     Task updatedTask = taskRepository.save(task);
    //     return mapToTaskResponse(updatedTask);
    // }

    private void redistributeTaskOrderPositions(Long groupId) {
        List<Task> tasks = taskRepository.findByKanbanGroupIdOrderByOrderPositionAsc(groupId);
        if (tasks.isEmpty()) {
            return;
        }
        String currentPosition = LexoRank.initialRank();
        for (Task t : tasks) {
            t.setOrderPosition(currentPosition);
            currentPosition = LexoRank.calculateAfter(currentPosition);
        }
        taskRepository.saveAll(tasks);
    }

    private TaskResponse mapToTaskResponse(Task task) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setOrderPosition(task.getOrderPosition());
        response.setKanbanGroupId(task.getKanbanGroup().getId());
        response.setAuthorId(task.getAuthor().getId());
        response.setDueDate(task.getDueDate());
        response.setPriority(task.getPriority());
        response.setCompleted(task.isCompleted());
        response.setColorId(task.getColor() != null ? task.getColor().getId() : null);
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());
        return response;
    }

    public void deleteTask(Long groupId, Long taskId, Long userId) {
        KanbanGroup kanbanGroup = kanbanGroupRepository.findById(groupId)
                .orElseThrow(() -> new KanbanGroupNotFoundException("Kanban group not found with id: " + groupId));

        if (!kanbanGroup.getProject().getOwnerId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this group");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        taskRepository.delete(task);
    }

}