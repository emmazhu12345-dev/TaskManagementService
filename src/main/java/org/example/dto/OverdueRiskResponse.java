package org.example.dto;

/** DTO for AI-predicted overdue risk for a task. */
public record OverdueRiskResponse(
        Long taskId,
        String taskTitle,
        double riskScore, // 0.0 to 1.0
        String riskLevel // "LOW", "MEDIUM", "HIGH"
        ) {
    public static OverdueRiskResponse fromTask(Long taskId, String taskTitle, double riskScore) {
        String riskLevel;
        if (riskScore >= 0.7) {
            riskLevel = "HIGH";
        } else if (riskScore >= 0.4) {
            riskLevel = "MEDIUM";
        } else {
            riskLevel = "LOW";
        }
        return new OverdueRiskResponse(taskId, taskTitle, riskScore, riskLevel);
    }
}
