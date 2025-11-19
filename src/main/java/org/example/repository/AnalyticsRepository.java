package org.example.repository;

import org.example.kafka.event.TaskRemovalReason;
import org.example.model.TaskDailyStats;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Repository for writing aggregated analytics data.
 * This layer defines business-level persistence operations.
 */
public interface AnalyticsRepository {

    void incrementDailyCreatedCount(LocalDate date);

    void incrementDailyCompletedCount(LocalDate date);

    void incrementDailyRemovedCount(LocalDate date, TaskRemovalReason reason);

    Optional<TaskDailyStats> findDailyStats(LocalDate date);
}