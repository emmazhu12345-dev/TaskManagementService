package org.example.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.example.kafka.event.TaskRemovalReason;
import org.example.model.TaskDailyStats;

/**
 * Repository for writing aggregated analytics data. This layer defines business-level persistence
 * operations.
 */
public interface AnalyticsRepository {

    void incrementDailyCreatedCount(LocalDate date);

    void incrementDailyCompletedCount(LocalDate date);

    void incrementDailyRemovedCount(LocalDate date, TaskRemovalReason reason);

    Optional<TaskDailyStats> findDailyStats(LocalDate date);

    List<TaskDailyStats> findStatsByDateRange(LocalDate startDate, LocalDate endDate);
}
