package org.example.dto;

import org.example.model.TaskPriority;

import java.time.Instant;

public record CreateTaskRequest(
        String title,
        String description,
        TaskPriority priority,
        Instant dueDate
) {}
