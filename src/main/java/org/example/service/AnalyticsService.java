package org.example.service;

import java.time.LocalDate;
import java.util.Optional;
import org.example.dto.TaskDailyStatsResponse;
import org.example.kafka.event.TaskRemovalReason;
import org.example.model.TaskDailyStats;
import org.example.repository.AnalyticsRepository;
import org.example.utils.TaskDailyStatsMapper;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {

    private final AnalyticsRepository repository;

    public AnalyticsService(AnalyticsRepository repository) {
        this.repository = repository;
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
}
