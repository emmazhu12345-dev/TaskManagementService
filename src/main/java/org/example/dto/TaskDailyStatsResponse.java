package org.example.dto;

import java.time.Instant;
import java.time.LocalDate;

/** DTO used by the REST API to return daily task statistics. */
public record TaskDailyStatsResponse(
        LocalDate statDate,
        long createdCount,
        long completedCount,
        long removedTotalCount,
        long removedDeletedCount,
        long removedCanceledCount,
        Instant createdAt,
        Instant updatedAt) {}
