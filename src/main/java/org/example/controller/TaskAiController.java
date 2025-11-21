package org.example.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.dto.*;
import org.example.model.AppUser;
import org.example.model.Task;
import org.example.model.WorkPatternInsight;
import org.example.service.AiTaskService;
import org.example.service.AnalyticsService;
import org.example.service.TaskService;
import org.example.service.UserService;
import org.example.utils.TaskMapper;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/tasks/ai")
@RequiredArgsConstructor
public class TaskAiController {

    private final AiTaskService aiTaskService;
    private final TaskService taskService;
    private final UserService userService;
    private final AnalyticsService analyticsService;

    // ===================================================
    // Option A: AI Productivity Summary
    // ===================================================
    @GetMapping("/summary")
    public ResponseEntity<ProductivitySummaryResponse> getDailySummary(
            Principal principal,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate date) {
        AppUser user = userService.loadByUsername(principal.getName());

        // Default to today if not specified
        LocalDate targetDate = (date == null) ? LocalDate.now() : date;

        // Get daily stats
        var stats = analyticsService
                .getDailyStats(targetDate)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No stats found for date: " + targetDate));

        // Generate AI summary
        String summary = aiTaskService.generateDailySummary(user, stats);

        return ResponseEntity.ok(new ProductivitySummaryResponse(summary));
    }

    // ===================================================
    // Option B: AI Predict Overdue Risk
    // ===================================================
    @GetMapping("/overdue-risk")
    public ResponseEntity<List<OverdueRiskResponse>> getOverdueRisks(Principal principal) {
        AppUser user = userService.loadByUsername(principal.getName());

        // Get all open tasks
        List<Task> openTasks = taskService.findOpenTasks(user.getId());

        // Predict risk for each task
        List<OverdueRiskResponse> risks = openTasks.stream()
                .map(task -> {
                    double riskScore = aiTaskService.predictOverdueRisk(user, task);
                    return OverdueRiskResponse.fromTask(task.getId(), task.getTitle(), riskScore);
                })
                .sorted((a, b) -> Double.compare(b.riskScore(), a.riskScore())) // Sort by risk descending
                .collect(Collectors.toList());

        return ResponseEntity.ok(risks);
    }

    @GetMapping("/overdue-risk/{taskId}")
    public ResponseEntity<OverdueRiskResponse> getOverdueRiskForTask(Principal principal, @PathVariable Long taskId) {
        AppUser user = userService.loadByUsername(principal.getName());

        // Get the task entity directly from repository
        var task = taskService.getTaskEntity(user.getId(), taskId);

        // Predict risk
        double riskScore = aiTaskService.predictOverdueRisk(user, task);

        return ResponseEntity.ok(OverdueRiskResponse.fromTask(taskId, task.getTitle(), riskScore));
    }

    // ===================================================
    // Option C: AI Recommend Task Priority (Re-ranking)
    // ===================================================
    @GetMapping("/recommendation")
    public ResponseEntity<List<TaskResponse>> getAiRecommendedTasks(Principal principal) {
        AppUser user = userService.loadByUsername(principal.getName());

        // Load candidate tasks (tasks for tomorrow)
        List<Task> candidateTasks = taskService.findTasksForTomorrow(user);

        // Let AI rerank
        List<Task> ordered = aiTaskService.rerankTasks(user, candidateTasks);

        // Map to response DTOs
        List<TaskResponse> responses =
                ordered.stream().map(TaskMapper::toResponse).toList();

        return ResponseEntity.ok(responses);
    }

    // ===================================================
    // Option D: AI Work Pattern Mining
    // ===================================================
    @GetMapping("/patterns")
    public ResponseEntity<List<WorkPatternInsightResponse>> getWorkPatterns(
            Principal principal, @RequestParam(value = "days", defaultValue = "30") int days) {
        AppUser user = userService.loadByUsername(principal.getName());

        // Get historical stats (last N days)
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        List<org.example.model.TaskDailyStats> history = analyticsService.getStatsByDateRange(startDate, endDate);

        if (history.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        // Analyze patterns
        List<WorkPatternInsight> insights = aiTaskService.analyzePatterns(user, history);

        // Map to response DTOs
        List<WorkPatternInsightResponse> responses =
                insights.stream().map(WorkPatternInsightResponse::fromModel).toList();

        return ResponseEntity.ok(responses);
    }
}
