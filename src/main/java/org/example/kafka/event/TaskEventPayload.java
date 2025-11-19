package org.example.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskEventPayload {
    private Long taskId;
    private Long ownerId;        // 如果你有用户体系
    private String title;
    private String description;
    private String status;
    private String priority;
    private Instant dueDate;
    private Instant createdAt;
    private Instant updatedAt;
    private TaskRemovalReason removalReason;
}
