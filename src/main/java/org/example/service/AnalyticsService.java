package org.example.service;

import java.time.LocalDate;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.TaskDailyStatsResponse;
import org.example.kafka.event.TaskEvent;
import org.example.kafka.event.TaskRemovalReason;
import org.example.model.AppUser;
import org.example.model.TaskDailyStats;
import org.example.repository.AnalyticsRepository;
import org.example.utils.TaskDailyStatsMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AnalyticsService {

    private final AnalyticsRepository repository;
    private final AiTaskService aiTaskService;
    private final UserService userService;

    public AnalyticsService(AnalyticsRepository repository, AiTaskService aiTaskService, UserService userService) {
        this.repository = repository;
        this.aiTaskService = aiTaskService;
        this.userService = userService;
    }

    public void recordTaskCreated() {
        repository.incrementDailyCreatedCount(LocalDate.now());
    }

    public void recordTaskCompleted() {
        repository.incrementDailyCompletedCount(LocalDate.now());
    }

    public void recordTaskRemoved(TaskRemovalReason reason) {
        repository.incrementDailyRemovedCount(LocalDate.now(), reason);
    }

    public TaskDailyStatsResponse getDailyStatsResponse(LocalDate date) {
        TaskDailyStats stats = repository
                .findDailyStats(date)
                .orElseThrow(() -> new IllegalArgumentException("No stats found for date: " + date));

        return TaskDailyStatsMapper.toResponse(stats);
    }

    public Optional<TaskDailyStats> getDailyStats(LocalDate date) {
        return repository.findDailyStats(date);
    }

    public java.util.List<TaskDailyStats> getStatsByDateRange(LocalDate startDate, LocalDate endDate) {
        return repository.findStatsByDateRange(startDate, endDate);
    }

    /**
     * Record daily stats and optionally generate AI summary. This method is called when a task is
     * completed to update statistics and trigger AI summary generation.
     *
     * <p>Note: AI summary generation is done asynchronously to avoid blocking the Kafka consumer.
     */
    @Async
    public void recordDailyStatsAndGenerateSummary(TaskEvent event) {
        // Record the task completion in daily stats
        recordTaskCompleted();

        // Asynchronously generate AI summary (non-blocking)
        generateAiSummaryAsync(event);
    }

    /**
    * Asynchronously generate AI daily summary for the user. This method runs in a separate thread
    * to avoid blocking the Kafka consumer.
    */
    private void generateAiSummaryAsync(TaskEvent event) {
        try {
            var payload = event.getPayload();
            if (payload == null || payload.getOwnerId() == null) {
                log.warn("[Analytics] Cannot generate AI summary: missing ownerId in event");
                return;
            }

            // Get user information
            AppUser user;
            try {
                user = userService.loadById(payload.getOwnerId());
            } catch (Exception e) {
                log.warn("[Analytics] Cannot generate AI summary: user not found with id={}", payload.getOwnerId(), e);
                return;
            }

            // Get today's stats
            LocalDate today = LocalDate.now();
            Optional<TaskDailyStats> statsOpt = getDailyStats(today);

            if (statsOpt.isEmpty()) {
                log.debug(
                        "[Analytics] No stats available for today (date={}), skipping AI summary generation",
                        today);
                return;
            }

            TaskDailyStats stats = statsOpt.get();

            // Generate AI summary
            try {
                String summary = aiTaskService.generateDailySummary(user, stats);
                log.info(
                        "[Analytics] Generated AI summary for user {} (date={}): {}",
                        user.getUsername(),
                        today,
                        summary);
                // In a real system, you might want to:
                // - Store the summary in a database
                // - Send it via email/push notification
                // - Cache it for the dashboard
            } catch (Exception e) {
                log.error("[Analytics] Failed to generate AI summary for user {}", user.getUsername(), e);
            }
        } catch (Exception e) {
            log.error("[Analytics] Error in async AI summary generation", e);
        }
    }
}
