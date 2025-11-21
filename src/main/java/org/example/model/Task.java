package org.example.model;

import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Task {

    private Long id;
    private Long ownerId;

    private String title;
    private String description;

    private TaskStatus status; // enum
    private TaskPriority priority; // enum

    private Instant dueDate;
    private Instant createdAt;
    private Instant updatedAt;
}
