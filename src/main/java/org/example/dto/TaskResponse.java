package org.example.dto;

import org.example.model.TaskPriority;
import org.example.model.TaskStatus;

import java.time.Instant;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        Instant dueDate,
        Instant createdAt,
        Instant updatedAt
) {}