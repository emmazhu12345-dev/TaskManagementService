package org.example.dto;

import java.time.Instant;
import org.example.model.TaskPriority;
import org.example.model.TaskStatus;

/** DTO for updating all editable fields of a Task. */
public record UpdateTaskRequest(
        String title, String description, TaskStatus status, TaskPriority priority, Instant dueDate) {}
