package org.example.dto;

import java.time.Instant;
import org.example.model.TaskPriority;

public record CreateTaskRequest(String title, String description, TaskPriority priority, Instant dueDate) {}
