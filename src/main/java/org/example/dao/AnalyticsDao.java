package org.example.dao;

import java.time.LocalDate;
import java.util.List;
import org.example.model.TaskDailyStats;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

/**
 * DAO for performing SQL updates to task_daily_stats. Uses PostgreSQL UPSERT (INSERT ... ON
 * CONFLICT).
 */
@RegisterBeanMapper(TaskDailyStats.class)
public interface AnalyticsDao {

    @SqlUpdate(
            """
                          INSERT INTO task_daily_stats (
                              stat_date,
                              created_count
                          )
                          VALUES (:date, 1)
                          ON CONFLICT (stat_date)
                          DO UPDATE SET
                              created_count = task_daily_stats.created_count + 1,
                              updated_at = now()
                          """)
    void incrementDailyCreatedCount(LocalDate date);

    @SqlUpdate(
            """
                          INSERT INTO task_daily_stats (
                              stat_date,
                              completed_count
                          )
                          VALUES (:date, 1)
                          ON CONFLICT (stat_date)
                          DO UPDATE SET
                              completed_count = task_daily_stats.completed_count + 1,
                              updated_at = now()
                          """)
    void incrementDailyCompletedCount(LocalDate date);

    @SqlUpdate(
            """
                          INSERT INTO task_daily_stats (
                              stat_date,
                              removed_total_count,
                              removed_deleted_count,
                              removed_canceled_count
                          )
                          VALUES (:date, 1, :deletedInc, :canceledInc)
                          ON CONFLICT (stat_date)
                          DO UPDATE SET
                              removed_total_count = task_daily_stats.removed_total_count + 1,
                              removed_deleted_count = task_daily_stats.removed_deleted_count + :deletedInc,
                              removed_canceled_count = task_daily_stats.removed_canceled_count + :canceledInc,
                              updated_at = now()
                          """)
    void incrementDailyRemovedCount(LocalDate date, int deletedInc, int canceledInc);

    @SqlQuery(
            """
                          SELECT
                              stat_date,
                              created_count,
                              completed_count,
                              removed_total_count,
                              removed_deleted_count,
                              removed_canceled_count,
                              created_at,
                              updated_at
                          FROM task_daily_stats
                          WHERE stat_date = :date
                          """)
    TaskDailyStats findStatsByDate(LocalDate date);

    @SqlQuery(
            """
                          SELECT
                              stat_date,
                              created_count,
                              completed_count,
                              removed_total_count,
                              removed_deleted_count,
                              removed_canceled_count,
                              created_at,
                              updated_at
                          FROM task_daily_stats
                          WHERE stat_date >= :startDate AND stat_date <= :endDate
                          ORDER BY stat_date ASC
                          """)
    List<TaskDailyStats> findStatsByDateRange(LocalDate startDate, LocalDate endDate);
}
