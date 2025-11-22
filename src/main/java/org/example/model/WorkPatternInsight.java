package org.example.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WorkPatternInsight {
    private String type;
    private String description;
    private Instant createdAt;
}
