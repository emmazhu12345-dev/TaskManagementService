package org.example.kafka.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class TaskEvent {

    private String eventId;          // UUID
    private TaskEventType type;      // CREATED / UPDATED / ...
    private Instant occurredAt;      // timestamp
    private String source;           // "tms-api"
    private String schemaVersion;    // "v1"
    private TaskEventPayload payload;

    public TaskEvent(TaskEventType type,
                     TaskEventPayload payload) {
        this.eventId = UUID.randomUUID().toString();
        this.type = type;
        this.occurredAt = Instant.now();
        this.source = "tms-api";
        this.schemaVersion = "v1";
        this.payload = payload;
    }
}
