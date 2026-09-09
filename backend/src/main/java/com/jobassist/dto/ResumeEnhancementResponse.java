package com.jobassist.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for resume enhancement results
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeEnhancementResponse {

    private Long sessionId;
    private int originalAtsScore;
    private int enhancedAtsScore;
    private int scoreImprovement;
    private List<String> addedKeywords;
    private List<ProjectInfo> addedProjects;
    private String enhancedResumeUrl;
    private int processingTimeMs;
    private Map<String, Object> scoreBreakdown;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectInfo {
        private Long id;
        private String title;
        private String category;
    }
}
