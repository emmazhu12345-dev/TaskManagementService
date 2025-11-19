package org.example.kafka.producer;

import lombok.extern.slf4j.Slf4j;
import org.example.kafka.event.*;
import org.example.model.Task;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes TaskEvent messages to Kafka.
 * Encapsulates event building and sending logic.
 */
@Component
@Slf4j
public class TaskEventProducer {

    private static final String TASK_EVENTS_TOPIC = "tms.task.events.v1";

    private final KafkaTemplate<String, TaskEvent> kafkaTemplate;

    public TaskEventProducer(KafkaTemplate<String, TaskEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishTaskCreated(Task task) {
        TaskEvent event = buildEvent(task, TaskEventType.TASK_CREATED, null);
        send(task.getId(), event);
    }

    public void publishTaskUpdated(Task task) {
        TaskEvent event = buildEvent(task, TaskEventType.TASK_UPDATED, null);
        send(task.getId(), event);
    }

    public void publishTaskCompleted(Task task) {
        TaskEvent event = buildEvent(task, TaskEventType.TASK_COMPLETED, null);
        send(task.getId(), event);
    }

    public void publishTaskRemoved(Task task, TaskRemovalReason reason) {
        TaskEvent event = buildEvent(task, TaskEventType.TASK_REMOVED, reason);
        send(task.getId(), event);
    }

    private void send(Long taskId, TaskEvent event) {
        String key = taskId == null ? null : taskId.toString();
        kafkaTemplate.send(TASK_EVENTS_TOPIC, key, event);
        log.info("Published TaskEvent: type={}, taskId={}", event.getType(), taskId);
    }

    private TaskEvent buildEvent(Task task,
                                 TaskEventType type,
                                 TaskRemovalReason removalReason) {

        TaskEventPayload payload = TaskEventPayload.builder()
                .taskId(task.getId())
                .ownerId(task.getOwnerId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus().name())
                .priority(task.getPriority().name())
                .dueDate(task.getDueDate())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .removalReason(removalReason)
                .build();

        return new TaskEvent(type, payload);
    }
}
