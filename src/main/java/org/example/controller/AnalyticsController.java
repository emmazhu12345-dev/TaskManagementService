package org.example.controller;

import org.example.dto.TaskDailyStatsResponse;
import org.example.service.AnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

/**
 * REST controller exposing analytics endpoints.
 */
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * GET /api/analytics/daily?date=2025-11-18
     * Returns aggregated stats for a specific date.
     */
    @GetMapping("/daily")
    public ResponseEntity<TaskDailyStatsResponse> getDailyStats(
            @RequestParam("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        try {
            TaskDailyStatsResponse response = analyticsService.getDailyStatsResponse(date);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }
}
