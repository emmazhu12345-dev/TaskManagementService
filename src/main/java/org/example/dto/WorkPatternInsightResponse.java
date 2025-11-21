package org.example.dto;

import java.time.Instant;
import org.example.model.WorkPatternInsight;

/** DTO for AI-analyzed work pattern insights. */
public record WorkPatternInsightResponse(String type, String description, Instant createdAt) {
    public static WorkPatternInsightResponse fromModel(WorkPatternInsight insight) {
        return new WorkPatternInsightResponse(insight.getType(), insight.getDescription(), insight.getCreatedAt());
    }
}
