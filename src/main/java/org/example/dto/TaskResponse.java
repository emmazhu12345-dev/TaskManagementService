package org.example.dto;

import java.time.Instant;
import org.example.model.TaskPriority;
import org.example.model.TaskStatus;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        Instant dueDate,
        Instant createdAt,
        Instant updatedAt) {}
