package org.example.service;

import org.example.dto.CreateTaskRequest;
import org.example.dto.TaskResponse;
import org.example.model.Task;
import org.example.model.TaskStatus;
import org.example.repository.TaskRepository;
import org.example.utils.TaskMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Service layer for Task business logic.
 * Contains the full implementation for createTask().
 * Other methods are defined as placeholders (TODO) for future expansion.
 */
@Service
public class TaskService {

    private final TaskRepository taskRepository;  // Or TaskRepositoryImpl based on your project name

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // ===================================================
    // Fully implemented: Create a new Task for the user
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
        return TaskMapper.toResponse(saved);
    }

    // ===========================================
    // TODO Methods (used by controller skeletons)
    // ===========================================

    /**
     * List all tasks belonging to the given owner.
     */
    public List<TaskResponse> listTasks(long ownerId) {
        // TODO: fetch tasks from taskRepository and map to TaskResponse
        throw new UnsupportedOperationException("TODO: implement listTasks");
    }

    /**
     * Fetch a single task by id + ownerId.
     */
    public TaskResponse getTask(long ownerId, long taskId) {
        // TODO: fetch task and convert to TaskResponse
        throw new UnsupportedOperationException("TODO: implement getTask");
    }

    /**
     * Update all editable fields of a task.
     * Requires an UpdateTaskRequest DTO.
     */
    public TaskResponse updateTask(long ownerId, long taskId /*, UpdateTaskRequest request */) {
        // TODO: lookup -> modify fields -> save -> return response
        throw new UnsupportedOperationException("TODO: implement updateTask");
    }

    /**
     * Update only the status of a task.
     */
    public TaskResponse updateTaskStatus(long ownerId, long taskId /*, TaskStatus newStatus */) {
        // TODO: update only the status field
        throw new UnsupportedOperationException("TODO: implement updateTaskStatus");
    }

    /**
     * Delete a task by id + ownerId.
     */
    public void deleteTask(long ownerId, long taskId) {
        // TODO: call repository to delete
        throw new UnsupportedOperationException("TODO: implement deleteTask");
    }
}
