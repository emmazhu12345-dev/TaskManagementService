// src/main/java/org/example/service/TaskService.java
package org.example.service;

import org.example.dto.CreateTaskRequest;
import org.example.dto.PagedResponse;
import org.example.dto.TaskResponse;
import org.example.dto.UpdateTaskRequest;
import org.example.dto.UpdateTaskStatusRequest;
import org.example.model.Task;
import org.example.model.TaskStatus;
import org.example.repository.TaskRepository;
import org.example.utils.TaskMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Service layer for Task business logic.
 */
@Service
public class TaskService {

    private final TaskRepository taskRepository;  // Or TaskRepositoryImpl

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // ===================================================
    // Create a new Task for the user
    // ===================================================
    @Transactional
    public TaskResponse createTask(long ownerId, CreateTaskRequest request) {
        Instant now = Instant.now();

        Task task = new Task();
        task.setOwnerId(ownerId);
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setPriority(request.priority());
        task.setStatus(TaskStatus.OPEN); // Default status
        task.setDueDate(request.dueDate());
        task.setCreatedAt(now);
        task.setUpdatedAt(now);

        Task saved = taskRepository.createTask(task);
        return toResponse(saved);
    }

    // ===================================================
    // List all tasks for the user
    // ===================================================
    public PagedResponse<TaskResponse> listTasksForUser(long ownerId, Pageable pageable) {
        PagedResponse<Task> pageResult = taskRepository.findTasksByOwner(ownerId, pageable);

        List<TaskResponse> content = pageResult.content().stream()
                .map(TaskMapper::toResponse)
                .toList();

        return new PagedResponse<>(
                content,
                pageResult.page(),
                pageResult.size(),
                pageResult.totalElements(),
                pageResult.totalPages(),
                pageResult.hasNext(),
                pageResult.hasPrevious()
        );
    }

    // ===================================================
    // Get a single task
    // ===================================================
    @Transactional(readOnly = true)
    public TaskResponse getTask(long ownerId, long taskId) {
        Task task = taskRepository.findTaskByIdAndOwner(taskId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        return toResponse(task);
    }

    // ===================================================
    // Update all editable fields
    // ===================================================
    @Transactional
    public TaskResponse updateTask(long ownerId, long taskId, UpdateTaskRequest request) {
        Task task = taskRepository.findTaskByIdAndOwner(taskId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setPriority(request.priority());
        task.setDueDate(request.dueDate());
        task.setUpdatedAt(Instant.now());

        boolean updated = taskRepository.updateTask(task);
        if (!updated) {
            throw new IllegalStateException("Failed to update task");
        }
        return toResponse(task);
    }

    // ===================================================
    // Update only status
    // ===================================================
    @Transactional
    public TaskResponse updateTaskStatus(long ownerId, long taskId, UpdateTaskStatusRequest request) {
        Task task = taskRepository.findTaskByIdAndOwner(taskId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        task.setStatus(request.status());
        task.setUpdatedAt(Instant.now());

        boolean updated = taskRepository.updateTask(task);
        if (!updated) {
            throw new IllegalStateException("Failed to update task status");
        }
        return toResponse(task);
    }

    // ===================================================
    // Delete a task
    // ===================================================
    @Transactional
    public void deleteTask(long ownerId, long taskId) {
        boolean deleted = taskRepository.deleteTask(taskId, ownerId);
        if (!deleted) {
            throw new IllegalArgumentException("Task not found");
        }
    }

    // ===================================================
    // Utility: Entity -> DTO
    // ===================================================
    private TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
