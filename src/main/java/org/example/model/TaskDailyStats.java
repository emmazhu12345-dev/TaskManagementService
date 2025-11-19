package org.example.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jdbi.v3.core.mapper.reflect.ColumnName;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
/**
 * Domain model representing daily aggregated task statistics.
 */
public class TaskDailyStats {

    @ColumnName("stat_date")
    private LocalDate statDate;

    @ColumnName("created_count")
    private long createdCount;

    @ColumnName("completed_count")
    private long completedCount;

    @ColumnName("removed_total_count")
    private long removedTotalCount;

    @ColumnName("removed_deleted_count")
    private long removedDeletedCount;

    @ColumnName("removed_canceled_count")
    private long removedCanceledCount;

    @ColumnName("created_at")
    private Instant createdAt;

    @ColumnName("updated_at")
    private Instant updatedAt;
}
