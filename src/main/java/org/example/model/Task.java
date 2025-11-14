package org.example.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
public class Task {

    private Long id;
    private Long ownerId;

    private String title;
    private String description;

    private TaskStatus status;     // enum
    private TaskPriority priority; // enum

    private Instant dueDate;
    private Instant createdAt;
    private Instant updatedAt;
}
