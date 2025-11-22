package org.example.utils;

import org.example.dto.TaskDailyStatsResponse;
import org.example.model.TaskDailyStats;

/** Mapper for converting TaskDailyStats domain model into TaskDailyStatsResponse DTO. */
public class TaskDailyStatsMapper {

    public static TaskDailyStatsResponse toResponse(TaskDailyStats stats) {
        return new TaskDailyStatsResponse(
                stats.getStatDate(),
                stats.getCreatedCount(),
                stats.getCompletedCount(),
                stats.getRemovedTotalCount(),
                stats.getRemovedDeletedCount(),
                stats.getRemovedCanceledCount(),
                stats.getCreatedAt(),
                stats.getUpdatedAt());
    }
}
