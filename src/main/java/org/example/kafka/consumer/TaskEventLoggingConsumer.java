package org.example.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.example.kafka.event.TaskEvent;
import org.example.kafka.event.TaskEventType;
import org.example.kafka.event.TaskRemovalReason;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Generic consumer that logs all TaskEvent messages.
 * Useful for demos, debugging, and teaching how Kafka consumers work.
 */
@Service
@Slf4j
public class TaskEventLoggingConsumer {

    @KafkaListener(
            topics = "tms.task.events.v1",
            groupId = "tms-event-logger",
            containerFactory = "taskEventListenerFactory"
    )
    public void onMessage(TaskEvent event) {
        if (event == null || event.getPayload() == null) {
            log.warn("Received null TaskEvent or null payload");
            return;
        }

        switch (event.getType()) {
            case TASK_CREATED -> handleTaskCreated(event);
            case TASK_UPDATED -> handleTaskUpdated(event);
            case TASK_COMPLETED -> handleTaskCompleted(event);
            case TASK_REMOVED -> handleTaskRemoved(event);
            default -> log.warn("Received unknown event type: {}", event.getType());
        }
    }

    private void handleTaskCreated(TaskEvent event) {
        var p = event.getPayload();
        log.info("[Event] TASK_CREATED — taskId={}, ownerId={}, title={}",
                p.getTaskId(), p.getOwnerId(), p.getTitle());
    }

    private void handleTaskUpdated(TaskEvent event) {
        var p = event.getPayload();
        log.info("[Event] TASK_UPDATED — taskId={}, status={}, priority={}",
                p.getTaskId(), p.getStatus(), p.getPriority());
    }

    private void handleTaskCompleted(TaskEvent event) {
        var p = event.getPayload();
        log.info("[Event] TASK_COMPLETED — taskId={}, completedAt={}",
                p.getTaskId(), p.getUpdatedAt());
    }

    private void handleTaskRemoved(TaskEvent event) {
        var p = event.getPayload();
        TaskRemovalReason reason = p.getRemovalReason();

        log.info("[Event] TASK_REMOVED — taskId={}, ownerId={}, reason={}",
                p.getTaskId(), p.getOwnerId(), reason);

        // Optional additional logging
        if (reason == TaskRemovalReason.DELETED) {
            log.debug("Task {} was permanently deleted", p.getTaskId());
        } else if (reason == TaskRemovalReason.CANCELED) {
            log.debug("Task {} was canceled by user {}", p.getTaskId(), p.getOwnerId());
        }
    }
}
