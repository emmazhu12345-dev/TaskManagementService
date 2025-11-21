package org.example.repository.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.example.dao.AnalyticsDao;
import org.example.kafka.event.TaskRemovalReason;
import org.example.model.TaskDailyStats;
import org.example.repository.AnalyticsRepository;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Repository;

/**
 * Repository implementation for aggregated analytics. Delegates SQL work to AnalyticsDao using
 * jdbi.withExtension, following the same style as TaskRepositoryImpl.
 */
@Repository
public class AnalyticsRepositoryImpl implements AnalyticsRepository {

    private final Jdbi jdbi;

    public AnalyticsRepositoryImpl(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public void incrementDailyCreatedCount(LocalDate date) {
        jdbi.useExtension(AnalyticsDao.class, dao -> dao.incrementDailyCreatedCount(date));
    }

    @Override
    public void incrementDailyCompletedCount(LocalDate date) {
        jdbi.useExtension(AnalyticsDao.class, dao -> dao.incrementDailyCompletedCount(date));
    }

    @Override
    public void incrementDailyRemovedCount(LocalDate date, TaskRemovalReason reason) {

        int deletedInc;
        int canceledInc;

        if (reason == TaskRemovalReason.DELETED) {
            canceledInc = 0;
            deletedInc = 1;
        } else {
            deletedInc = 0;
            if (reason == TaskRemovalReason.CANCELED) {
                canceledInc = 1;
            } else {
                canceledInc = 0;
            }
        }

        jdbi.useExtension(AnalyticsDao.class, dao -> dao.incrementDailyRemovedCount(date, deletedInc, canceledInc));
    }

    @Override
    public Optional<TaskDailyStats> findDailyStats(LocalDate date) {
        return jdbi.withExtension(AnalyticsDao.class, dao -> Optional.ofNullable(dao.findStatsByDate(date)));
    }

    @Override
    public List<TaskDailyStats> findStatsByDateRange(LocalDate startDate, LocalDate endDate) {
        return jdbi.withExtension(AnalyticsDao.class, dao -> dao.findStatsByDateRange(startDate, endDate));
    }
}
