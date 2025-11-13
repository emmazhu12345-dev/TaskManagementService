package org.example.controller;

import org.example.auth.CustomUserPrincipal;
import org.example.dto.CreateTaskRequest;
import org.example.dto.TaskResponse;
import org.example.service.TaskService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Task-related endpoints.
 * Only createTask is fully implemented.
 * All other endpoints are skeletons with TODO placeholders.
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // ======================================
    // 1) Fully Implemented: Create a new Task
    // ======================================
    @PostMapping
    public TaskResponse createTask(
            @AuthenticationPrincipal CustomUserPrincipal user,
            @RequestBody CreateTaskRequest request
    ) {
        long ownerId = user.getId();
        return taskService.createTask(ownerId, request);
    }

    // ===========================
    // 2) Other endpoints (TODOs)
    // ===========================

    /**
     * List all tasks belonging to the authenticated user.
     */
    @GetMapping
    public List<TaskResponse> listTasks(
            @AuthenticationPrincipal CustomUserPrincipal user
    ) {
        // TODO: call taskService.listTasks(ownerId)
        throw new UnsupportedOperationException("TODO: implement listTasks endpoint");
    }

    /**
     * Get a specific task by its ID.
     */
    @GetMapping("/{id}")
    public TaskResponse getTask(
            @AuthenticationPrincipal CustomUserPrincipal user,
            @PathVariable long id
    ) {
        // TODO: call taskService.getTask(ownerId, id)
        throw new UnsupportedOperationException("TODO: implement getTask endpoint");
    }

    /**
     * Update all editable fields of a task.
     * Requires defining UpdateTaskRequest DTO.
     */
    @PutMapping("/{id}")
    public TaskResponse updateTask(
            @AuthenticationPrincipal CustomUserPrincipal user,
            @PathVariable long id
            // @RequestBody UpdateTaskRequest request
    ) {
        // TODO: define UpdateTaskRequest and call taskService.updateTask(...)
        throw new UnsupportedOperationException("TODO: implement updateTask endpoint");
    }

    /**
     * Update only the status of a task.
     * Requires defining UpdateTaskStatusRequest DTO.
     */
    @PatchMapping("/{id}/status")
    public TaskResponse updateTaskStatus(
            @AuthenticationPrincipal CustomUserPrincipal user,
            @PathVariable long id
            // @RequestBody UpdateTaskStatusRequest request
    ) {
        // TODO: define UpdateTaskStatusRequest and call taskService.updateTaskStatus(...)
        throw new UnsupportedOperationException("TODO: implement updateTaskStatus endpoint");
    }

    /**
     * Delete a task by its ID.
     */
    @DeleteMapping("/{id}")
    public void deleteTask(
            @AuthenticationPrincipal CustomUserPrincipal user,
            @PathVariable long id
    ) {
        // TODO: call taskService.deleteTask(ownerId, id)
        throw new UnsupportedOperationException("TODO: implement deleteTask endpoint");
    }
}
