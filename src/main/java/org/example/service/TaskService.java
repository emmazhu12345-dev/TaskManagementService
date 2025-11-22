// src/main/java/org/example/service/TaskService.java
package org.example.service;

import java.time.Instant;
import java.util.List;
import org.example.dto.CreateTaskRequest;
import org.example.dto.PagedResponse;
import org.example.dto.TaskResponse;
import org.example.dto.UpdateTaskRequest;
import org.example.dto.UpdateTaskStatusRequest;
import org.example.kafka.event.TaskRemovalReason;
import org.example.kafka.producer.TaskEventProducer;
import org.example.model.Task;
import org.example.model.TaskStatus;
import org.example.repository.TaskRepository;
import org.example.utils.TaskMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Service layer for Task business logic. */
@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskEventProducer taskEventProducer;

    public TaskService(TaskRepository taskRepository, TaskEventProducer taskEventProducer) {
        this.taskRepository = taskRepository;
        this.taskEventProducer = taskEventProducer;
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

        // Publish "task created" event
        taskEventProducer.publishTaskCreated(saved);

        return TaskMapper.toResponse(saved);
    }

    // ===================================================
    // List all tasks for the user
    // ===================================================
    public PagedResponse<TaskResponse> listTasksForUser(long ownerId, Pageable pageable) {
        PagedResponse<Task> pageResult = taskRepository.findTasksByOwner(ownerId, pageable);

        List<TaskResponse> content =
                pageResult.content().stream().map(TaskMapper::toResponse).toList();

        return new PagedResponse<>(
                content,
                pageResult.page(),
                pageResult.size(),
                pageResult.totalElements(),
                pageResult.totalPages(),
                pageResult.hasNext(),
                pageResult.hasPrevious());
    }

    // ===================================================
    // Get a single task
    // ===================================================
    @Transactional(readOnly = true)
    public TaskResponse getTask(long ownerId, long taskId) {
        Task task = taskRepository
                .findTaskByIdAndOwner(taskId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        return TaskMapper.toResponse(task);
    }

    // ===================================================
    // Update all editable fields
    // ===================================================
    @Transactional
    public TaskResponse updateTask(long ownerId, long taskId, UpdateTaskRequest request) {
        Task task = taskRepository
                .findTaskByIdAndOwner(taskId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

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

        // use the updated Task entity to publish event
        taskEventProducer.publishTaskUpdated(task);

        return TaskMapper.toResponse(task);
    }

    // ===================================================
    // Update only status
    // ===================================================
    @Transactional
    public TaskResponse updateTaskStatus(long ownerId, long taskId, UpdateTaskStatusRequest request) {
        Task task = taskRepository
                .findTaskByIdAndOwner(taskId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        task.setStatus(request.status());
        task.setUpdatedAt(Instant.now());

        boolean updated = taskRepository.updateTask(task);
        if (!updated) {
            throw new IllegalStateException("Failed to update task status");
        }

        // ---------------------------------------------------
        // Publish Kafka events based on the new task status
        // ---------------------------------------------------
        switch (request.status()) {
            case COMPLETED -> taskEventProducer.publishTaskCompleted(task);
            case CANCELLED -> taskEventProducer.publishTaskRemoved(task, TaskRemovalReason.CANCELED);
            default -> taskEventProducer.publishTaskUpdated(task);
        }

        return TaskMapper.toResponse(task);
    }

    // ===================================================
    // Delete a task
    // ===================================================
    @Transactional
    public void deleteTask(long ownerId, long taskId) {
        // Fetch the task first so we can publish a meaningful event
        Task task = taskRepository
                .findTaskByIdAndOwner(taskId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        boolean deleted = taskRepository.deleteTask(taskId, ownerId);
        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found");
        }
        // Publish a unified removal event (delete = hard removal)
        taskEventProducer.publishTaskRemoved(task, TaskRemovalReason.DELETED);
    }

    // ===================================================
    // Find tasks for tomorrow (for AI recommendation)
    // ===================================================
    public List<Task> findTasksForTomorrow(org.example.model.AppUser user) {
        // Tomorrow end of day: 23:59:59.999
        java.time.LocalDate tomorrow = java.time.LocalDate.now().plusDays(1);
        java.time.Instant tomorrowEnd = tomorrow.atTime(23, 59, 59, 999_999_999)
                .atZone(java.time.ZoneId.systemDefault())
                .toInstant();

        return taskRepository.findTasksForTomorrow(user.getId(), tomorrowEnd);
    }

    // ===================================================
    // Find all open tasks for a user
    // ===================================================
    public List<Task> findOpenTasks(long ownerId) {
        return taskRepository.findOpenTasksByOwner(ownerId);
    }

    // ===================================================
    // Get task entity (for AI services)
    // ===================================================
    @Transactional(readOnly = true)
    public Task getTaskEntity(long ownerId, long taskId) {
        return taskRepository
                .findTaskByIdAndOwner(taskId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    }
}
