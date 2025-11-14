// src/main/java/org/example/controller/TaskController.java
package org.example.controller;

import org.example.auth.CustomUserPrincipal;
import org.example.dto.CreateTaskRequest;
import org.example.dto.TaskResponse;
import org.example.dto.UpdateTaskRequest;
import org.example.dto.UpdateTaskStatusRequest;
import org.example.service.TaskService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Task-related endpoints.
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // ======================================
    // Create a new Task
    // ======================================
    @PostMapping
    public TaskResponse createTask(
            @AuthenticationPrincipal CustomUserPrincipal user,
            @RequestBody CreateTaskRequest request
    ) {
        long ownerId = user.getId();
        return taskService.createTask(ownerId, request);
    }

    // ======================================
    // List all Tasks for current user
    // ======================================
    @GetMapping
    public List<TaskResponse> listTasks(
            @AuthenticationPrincipal CustomUserPrincipal user
    ) {
        long ownerId = user.getId();
        return taskService.listTasks(ownerId);
    }

    // ======================================
    // Get a single Task
    // ======================================
    @GetMapping("/{id}")
    public TaskResponse getTask(
            @AuthenticationPrincipal CustomUserPrincipal user,
            @PathVariable long id
    ) {
        long ownerId = user.getId();
        return taskService.getTask(ownerId, id);
    }

    // ======================================
    // Update all editable fields of a Task
    // ======================================
    @PutMapping("/{id}")
    public TaskResponse updateTask(
            @AuthenticationPrincipal CustomUserPrincipal user,
            @PathVariable long id,
            @RequestBody UpdateTaskRequest request
    ) {
        long ownerId = user.getId();
        return taskService.updateTask(ownerId, id, request);
    }

    // ======================================
    // Update only Task status
    // ======================================
    @PatchMapping("/{id}/status")
    public TaskResponse updateTaskStatus(
            @AuthenticationPrincipal CustomUserPrincipal user,
            @PathVariable long id,
            @RequestBody UpdateTaskStatusRequest request
    ) {
        long ownerId = user.getId();
        return taskService.updateTaskStatus(ownerId, id, request);
    }

    // ======================================
    // Delete a Task
    // ======================================
    @DeleteMapping("/{id}")
    public void deleteTask(
            @AuthenticationPrincipal CustomUserPrincipal user,
            @PathVariable long id
    ) {
        long ownerId = user.getId();
        taskService.deleteTask(ownerId, id);
    }
}
