package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.example.model.AppUser;
import org.example.model.Task;
import org.example.model.TaskDailyStats;
import org.example.model.WorkPatternInsight;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AiTaskService {

    private final OpenAIClient openAIClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Use GPT-5.1 (or adjust to the model you want)
    private static final ChatModel MODEL = ChatModel.GPT_5_1;

    public AiTaskService(OpenAIClient openAIClient) {
        this.openAIClient = openAIClient;
    }

    // -----------------------
    // Function A: Daily Summary
    // -----------------------
    public String generateDailySummary(AppUser user, TaskDailyStats stats) {
        String prompt = buildDailySummaryPrompt(user, stats);

        try {
            return callChatCompletion(prompt).trim();
        } catch (Exception e) {
            log.error("Failed to generate daily summary", e);
            return fallbackDailySummary(stats);
        }
    }

    // -----------------------
    // Function B: Overdue Risk
    // -----------------------
    public double predictOverdueRisk(AppUser user, Task task) {
        String prompt = buildOverdueRiskPrompt(user, task);

        try {
            String raw = callChatCompletion(prompt);
            String cleaned = raw.replaceAll("[^0-9\\.]", "").trim();

            if (cleaned.isEmpty()) return 0.5;

            double value = Double.parseDouble(cleaned);
            return Math.max(0.0, Math.min(1.0, value));
        } catch (Exception e) {
            log.error("Failed to predict overdue risk", e);
            return 0.5;
        }
    }

    // -----------------------
    // Function C: Re-ranking
    // -----------------------
    public List<Task> rerankTasks(AppUser user, List<Task> tasks) {
        if (tasks.isEmpty()) {
            return tasks;
        }

        String prompt = buildRerankPrompt(user, tasks);

        try {
            String raw = callChatCompletion(prompt);
            List<Long> orderedIds = parseIdList(raw);

            Map<Long, Task> map = tasks.stream().collect(Collectors.toMap(Task::getId, t -> t));

            List<Task> ordered = new ArrayList<>();
            for (Long id : orderedIds) {
                Task t = map.get(id);
                if (t != null) ordered.add(t);
            }

            // Append tasks not mentioned by the model
            Set<Long> returnedIdSet = new HashSet<>(orderedIds);
            tasks.stream().filter(t -> !returnedIdSet.contains(t.getId())).forEach(ordered::add);

            return ordered;

        } catch (Exception e) {
            log.error("Failed to rerank tasks", e);
            return tasks;
        }
    }

    // -----------------------
    // Function D: Pattern Mining
    // -----------------------
    public List<WorkPatternInsight> analyzePatterns(AppUser user, List<TaskDailyStats> history) {
        if (history.isEmpty()) {
            return List.of();
        }

        String statsJson = serializeStats(history);
        String prompt = buildPatternPrompt(user, statsJson);

        try {
            String raw = callChatCompletion(prompt);

            WorkPatternInsight insight = new WorkPatternInsight("LLM_PATTERN_SUMMARY", raw.trim(), Instant.now());
            return List.of(insight);

        } catch (Exception e) {
            log.error("Failed to analyze patterns", e);
            return List.of();
        }
    }

    // -----------------------
    // Core LLM Client Call
    // -----------------------
    private String callChatCompletion(String prompt) {
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(MODEL)
                .addUserMessage(prompt)
                .build();

        ChatCompletion completion = openAIClient.chat().completions().create(params);

        return completion.choices().stream()
                .findFirst()
                .flatMap(choice -> choice.message().content())
                .orElseThrow(() -> new IllegalStateException("LLM returned empty response"));
    }

    // -----------------------
    // Prompt Builders (English)
    // -----------------------

    // Function A
    private String buildDailySummaryPrompt(AppUser user, TaskDailyStats stats) {
        return """
            You are a productivity assistant for a task management system.
            
            Write a concise 1–2 sentence English summary describing the user's productivity today,
            based on the provided daily statistics. Mention:
            - how many tasks were created,
            - how many were completed,
            - how many were removed (including deleted/canceled),
            - a brief comment on workload balance,
            - and give one concrete suggestion for tomorrow.
            
            Output plain English text only.
            
            User:
            - username: %s
            
            Daily Stats:
            - date: %s
            - created_count: %d
            - completed_count: %d
            - removed_total_count: %d
            - removed_deleted_count: %d
            - removed_canceled_count: %d
            """
                .formatted(
                    safe(user.getUsername()),
                    stats.getStatDate(),
                    stats.getCreatedCount(),
                    stats.getCompletedCount(),
                    stats.getRemovedTotalCount(),
                    stats.getRemovedDeletedCount(),
                    stats.getRemovedCanceledCount());
    }

    // Function B
    private String buildOverdueRiskPrompt(AppUser user, Task task) {
        return """
            You are an assistant that predicts overdue risk.
            
            Based on the task information below, return ONLY a number between 0.0 and 1.0
            representing the probability that this task will become overdue.
            
            Output ONLY the number. No explanation.
            
            User:
            - username: %s
            
            Task:
            - id: %d
            - title: %s
            - description: %s
            - priority: %s
            - status: %s
            - created_at: %s
            - due_date: %s
            """
                .formatted(
                    safe(user.getUsername()),
                    task.getId(),
                    safe(task.getTitle()),
                    safe(task.getDescription()),
                    String.valueOf(task.getPriority()),
                    String.valueOf(task.getStatus()),
                    task.getCreatedAt() != null
                            ? DateTimeFormatter.ISO_INSTANT.format(task.getCreatedAt())
                            : "unknown",
                    task.getDueDate() != null ? DateTimeFormatter.ISO_INSTANT.format(task.getDueDate()) : "none");
    }

    // Function C
    private String buildRerankPrompt(AppUser user, List<Task> tasks) {
        StringBuilder sb = new StringBuilder();

        sb.append("""
            You are an AI assistant that re-ranks tasks for a user.
            
            Given the task list below, reorder them in the recommended priority for tomorrow.
            Consider:
            - priority level
            - urgency (based on dueDate)
            - task status
            - title and description for contextual hints
            - task age (createdAt)
            - reasonable productivity patterns (e.g., do short tasks first, big tasks later)
            
            Output ONLY a comma-separated list of task IDs in the new order.
            Do NOT output any explanation or additional text.
            Example: 3,5,1,2
            
            User:
            """);

        sb.append("- username: ").append(safe(user.getUsername())).append("\n\n");

        sb.append("Tasks:\n");

        // Add each task in a consistent LLM-friendly format
        for (Task t : tasks) {
            sb.append("ID=")
                .append(t.getId())
                .append(", title=")
                .append(safe(t.getTitle()))
                .append(", description=")
                .append(safe(t.getDescription()))
                .append(", priority=")
                .append(t.getPriority())
                .append(", status=")
                .append(t.getStatus())
                .append(", dueDate=")
                .append(t.getDueDate() != null ? DateTimeFormatter.ISO_INSTANT.format(t.getDueDate()) : "null")
                .append(", createdAt=")
                .append(t.getCreatedAt() != null ? DateTimeFormatter.ISO_INSTANT.format(t.getCreatedAt()) : "null")
                .append("\n");
        }

        sb.append("\nReturn only the ID list.");
        return sb.toString();
    }

    // Function D
    private String buildPatternPrompt(AppUser user, String statsJson) {
        return """
            You are an AI assistant that analyzes long-term productivity patterns.
            
            You will receive a JSON list of daily task statistics.
            Identify 2–5 meaningful patterns about the user's work behavior,
            such as weekday trends, morning/afternoon efficiency differences, or backlog tendencies.
            
            Output multiple bullet points in English, plus a final 1–2 sentence overall recommendation.
            Do NOT output JSON.
            
            User:
            - username: %s
            
            Daily Stats JSON:
            %s
            """
                .formatted(safe(user.getUsername()), statsJson);
    }

    // -----------------------
    // Helpers
    // -----------------------

    private String serializeStats(List<TaskDailyStats> history) {
        try {
            return objectMapper.writeValueAsString(history);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<Long> parseIdList(String raw) {
        String[] parts = raw.split("[,\\s]+");
        List<Long> ids = new ArrayList<>();

        for (String p : parts) {
            if (p.isBlank()) continue;
            try {
                ids.add(Long.parseLong(p.trim()));
            } catch (Exception ignored) {
            }
        }
        return ids;
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String fallbackDailySummary(TaskDailyStats stats) {
        return "AI summary unavailable. Completed "
                + stats.getCompletedCount()
                + " tasks today. Created "
                + stats.getCreatedCount()
                + " new tasks.";
    }
}
