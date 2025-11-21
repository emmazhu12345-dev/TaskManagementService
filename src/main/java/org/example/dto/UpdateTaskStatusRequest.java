package org.example.dto;

import org.example.model.TaskStatus;

/** DTO for updating only the status of a Task. */
public record UpdateTaskStatusRequest(TaskStatus status) {}
