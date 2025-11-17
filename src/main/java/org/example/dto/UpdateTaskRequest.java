package org.example.dto;

import org.example.model.TaskPriority;
import org.example.model.TaskStatus;

import java.time.Instant;

/**
 * DTO for updating all editable fields of a Task.
 */
public record UpdateTaskRequest(
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        Instant dueDate
) {}
