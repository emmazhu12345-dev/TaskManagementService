package org.example.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.example.kafka.event.TaskEvent;
import org.example.kafka.event.TaskEventType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Consumer responsible for handling user-facing notifications.
 * In a real system, this would send emails, push notifications,
 * Slack messages, or in-app alerts.
 */
@Service
@Slf4j
public class TaskNotificationConsumer {

    @KafkaListener(
            topics = "tms.task.events.v1",
            groupId = "tms-notification-service",
            containerFactory = "taskEventListenerFactory"
    )
    public void onMessage(TaskEvent event) {

        if (event == null || event.getPayload() == null) {
            log.warn("[Notification] Received null TaskEvent or payload");
            return;
        }

        switch (event.getType()) {
            case TASK_CREATED -> handleTaskCreated(event);
            case TASK_COMPLETED -> handleTaskCompleted(event);
            case TASK_REMOVED -> handleTaskRemoved(event);

            // Notifications usually ignore general updates
            case TASK_UPDATED -> {
                // no-op
            }

            default -> log.warn("[Notification] Unknown event type {}", event.getType());
        }
    }

    private void handleTaskCreated(TaskEvent event) {
        var payload = event.getPayload();
        log.info("[Notification] Task CREATED → Notify user {} (taskId={}, title={})",
                payload.getOwnerId(), payload.getTaskId(), payload.getTitle());

        // Example:
        // notificationService.sendTaskCreatedEmail(payload.getOwnerId(), payload.getTaskId(), payload.getTitle());
    }

    private void handleTaskCompleted(TaskEvent event) {
        var payload = event.getPayload();
        log.info("[Notification] Task COMPLETED → Notify user {} (taskId={})",
                payload.getOwnerId(), payload.getTaskId());

        // notificationService.sendTaskCompletedEmail(payload.getOwnerId(), payload.getTaskId());
    }

    private void handleTaskRemoved(TaskEvent event) {
        var payload = event.getPayload();
        log.info("[Notification] Task REMOVED → Notify user {} (taskId={})",
                payload.getOwnerId(), payload.getTaskId());

        // notificationService.sendTaskRemovedAlert(payload.getOwnerId(), payload.getTaskId(), payload.getRemovalReason());
    }
}
