package org.example.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.example.kafka.event.TaskEvent;
import org.example.kafka.event.TaskEventType;
import org.example.kafka.event.TaskRemovalReason;
import org.example.service.AnalyticsService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Consumer responsible for analytics, metrics, and reporting. This consumer processes events and
 * aggregates business statistics.
 */
@Service
@Slf4j
public class TaskAnalyticsConsumer {

    private final AnalyticsService analyticsService;

    public TaskAnalyticsConsumer(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @KafkaListener(
            topics = "tms.task.events.v1",
            groupId = "tms-analytics-service",
            containerFactory = "taskEventListenerFactory")
    public void onMessage(TaskEvent event) {

        if (event == null || event.getPayload() == null) {
            log.warn("[Analytics] Received null TaskEvent or payload");
            return;
        }

        TaskEventType type = event.getType();

        switch (type) {
            case TASK_CREATED -> handleTaskCreated(event);
            case TASK_COMPLETED -> handleTaskCompleted(event);
            case TASK_REMOVED -> handleTaskRemoved(event);
            case TASK_UPDATED -> handleTaskUpdated(event); // optional tracking
            default -> log.warn("[Analytics] Unknown event type: {}", type);
        }
    }

    // ==========================================================
    // Handler functions with logging + analyticsService calls
    // ==========================================================

    private void handleTaskCreated(TaskEvent event) {
        var p = event.getPayload();
        log.info(
                "[Analytics] Handling TASK_CREATED — date={}, taskId={}, ownerId={}",
                p.getCreatedAt(),
                p.getTaskId(),
                p.getOwnerId());

        // Existing analytics aggregation
        analyticsService.recordTaskCreated();

        // If you want, you could also update daily stats here in the future.
        // analyticsService.updateDailyStats(event);
    }

    private void handleTaskCompleted(TaskEvent event) {
        var p = event.getPayload();
        log.info(
                "[Analytics] Handling TASK_COMPLETED — date={}, taskId={}, ownerId={}",
                p.getUpdatedAt(),
                p.getTaskId(),
                p.getOwnerId());

        // Existing analytics aggregation
        analyticsService.recordTaskCompleted();

        // New: update daily stats and trigger AI daily summary generation
        // This method will use AiTaskService internally.
        analyticsService.recordDailyStatsAndGenerateSummary(event);
    }

    private void handleTaskRemoved(TaskEvent event) {
        var p = event.getPayload();
        TaskRemovalReason reason = p.getRemovalReason();

        log.info(
                "[Analytics] Handling TASK_REMOVED — date={}, taskId={}, ownerId={}, reason={}",
                p.getUpdatedAt(),
                p.getTaskId(),
                p.getOwnerId(),
                reason);

        analyticsService.recordTaskRemoved(reason);
    }

    private void handleTaskUpdated(TaskEvent event) {
        var p = event.getPayload();
        log.info(
                "[Analytics] Handling TASK_UPDATED (no aggregation yet) — taskId={}, ownerId={}, status={}",
                p.getTaskId(),
                p.getOwnerId(),
                p.getStatus());

        // If you want to track update stats in the future,
        // you can add a method like:
        // analyticsService.recordTaskUpdated();
    }
}
