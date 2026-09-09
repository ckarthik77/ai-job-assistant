package com.jobassist.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobassist.model.JobDescription;
import com.jobassist.model.Project;
import com.jobassist.model.Resume;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for AI-powered resume enhancement using Claude API
 * Handles keyword extraction, skill matching, and XYZ format generation
 */
@Service
@Slf4j
public class AIEnhancementService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${claude.api.key}")
    private String apiKey;

    @Value("${claude.api.model}")
    private String model;

    @Value("${claude.api.max-tokens}")
    private int maxTokens;

    @Value("${claude.api.timeout}")
    private long timeoutMs;

    public AIEnhancementService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder
                .baseUrl("https://api.anthropic.com/v1")
                .build();
        this.objectMapper = objectMapper;
    }

    /**
     * Extract keywords and required skills from job description using Claude API
     */
    public List<String> extractJobKeywords(String jobDescriptionText) {
        String prompt = String.format("""
                Extract all technical skills, tools, frameworks, qualifications, and key requirements from this job description.
                Return ONLY a JSON array of strings, nothing else.

                Job Description:
                %s

                Format: ["skill1", "skill2", "skill3", ...]
                """, jobDescriptionText);

        try {
            String response = callClaudeAPI(prompt);
            // Parse JSON array from response
            return objectMapper.readValue(response, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.error("Error extracting job keywords with Claude API", e);
            // Fallback to basic extraction
            return extractKeywordsFallback(jobDescriptionText);
        }
    }

    /**
     * Match resume skills to job requirements and identify gaps
     */
    public Map<String, Object> matchSkillsToJob(List<String> resumeSkills, List<String> jobKeywords) {
        Set<String> resumeSkillSet = new HashSet<>(resumeSkills.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList()));

        List<String> matchedSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();

        for (String keyword : jobKeywords) {
            if (resumeSkillSet.contains(keyword.toLowerCase())) {
                matchedSkills.add(keyword);
            } else {
                missingSkills.add(keyword);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("matchedSkills", matchedSkills);
        result.put("missingSkills", missingSkills);
        result.put("matchPercentage", jobKeywords.isEmpty() ? 0 :
                (matchedSkills.size() * 100.0 / jobKeywords.size()));

        return result;
    }

    /**
     * Convert project description to Google XYZ format
     */
    public String generateXYZFormat(String projectDescription, List<String> skills) {
        String prompt = String.format("""
                Convert this project description into Google's XYZ format:
                "Accomplished [X] as measured by [Y], by doing [Z]"

                Project: %s
                Skills used: %s

                Requirements:
                - Keep it concise (1-2 sentences maximum)
                - Quantify impact where possible (use metrics, percentages, or numbers)
                - Focus on accomplishments and results
                - Return ONLY the formatted description, nothing else
                """, projectDescription, String.join(", ", skills));

        try {
            return callClaudeAPI(prompt).trim();
        } catch (Exception e) {
            log.error("Error generating XYZ format with Claude API", e);
            return projectDescription; // Return original if API fails
        }
    }

    /**
     * Enhance resume content by selecting relevant projects and adding keywords
     */
    public Map<String, Object> enhanceResumeContent(
            Resume resume,
            JobDescription jobDescription,
            List<Project> availableProjects
    ) {
        // Prepare context for Claude
        String resumeContext = String.format("""
                Resume Skills: %s
                Resume Experience: %s
                """,
                String.join(", ", resume.getSkills()),
                resume.getExperience().size() + " work experiences listed"
        );

        String jobContext = String.format("""
                Job Title: %s
                Company: %s
                Required Keywords: %s
                """,
                jobDescription.getJobTitle(),
                jobDescription.getCompanyName(),
                String.join(", ", jobDescription.getKeywords())
        );

        String projectsContext = availableProjects.stream()
                .map(p -> String.format("ID: %d | Title: %s | Skills: %s | Category: %s",
                        p.getId(), p.getTitle(), String.join(", ", p.getSkills()), p.getCategory()))
                .collect(Collectors.joining("\n"));

        String prompt = String.format("""
                You are a professional resume optimizer. Given this resume and job requirements, suggest enhancements.

                RESUME:
                %s

                JOB REQUIREMENTS:
                %s

                AVAILABLE PROJECTS (select 2-3 most relevant):
                %s

                Rules:
                1. Select 2-3 projects that best match the job requirements
                2. Suggest keywords to add naturally (no keyword stuffing)
                3. Ensure all content fits on ONE PAGE
                4. Use Google XYZ format for achievements
                5. Preserve existing relevant content

                Return ONLY a JSON object with this structure:
                {
                  "selectedProjectIds": [1, 5, 12],
                  "keywordsToAdd": ["keyword1", "keyword2"],
                  "recommendations": "Brief explanation of changes"
                }
                """, resumeContext, jobContext, projectsContext);

        try {
            String response = callClaudeAPI(prompt);
            // Parse JSON response
            JsonNode jsonResponse = objectMapper.readTree(response);

            Map<String, Object> result = new HashMap<>();
            result.put("selectedProjectIds", objectMapper.convertValue(
                    jsonResponse.get("selectedProjectIds"),
                    new TypeReference<List<Long>>() {}
            ));
            result.put("keywordsToAdd", objectMapper.convertValue(
                    jsonResponse.get("keywordsToAdd"),
                    new TypeReference<List<String>>() {}
            ));
            result.put("recommendations", jsonResponse.get("recommendations").asText());

            return result;
        } catch (Exception e) {
            log.error("Error enhancing resume with Claude API", e);
            // Fallback: select projects based on skill matching
            return enhanceResumeFallback(resume, jobDescription, availableProjects);
        }
    }

    /**
     * Call Claude API with the given prompt
     */
    private String callClaudeAPI(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("max_tokens", maxTokens);
        requestBody.put("messages", List.of(
                Map.of("role", "user", "content", prompt)
        ));

        try {
            String response = webClient.post()
                    .uri("/messages")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(timeoutMs))
                    .block();

            // Parse response and extract content
            JsonNode jsonResponse = objectMapper.readTree(response);
            JsonNode content = jsonResponse.get("content");
            if (content != null && content.isArray() && content.size() > 0) {
                return content.get(0).get("text").asText();
            }

            throw new RuntimeException("Invalid response format from Claude API");
        } catch (Exception e) {
            log.error("Error calling Claude API", e);
            throw new RuntimeException("Failed to call Claude API: " + e.getMessage(), e);
        }
    }

    /**
     * Fallback keyword extraction without AI
     */
    private List<String> extractKeywordsFallback(String text) {
        // Simple keyword extraction based on common patterns
        List<String> keywords = new ArrayList<>();
        String[] words = text.split("\\W+");

        for (String word : words) {
            if (word.length() > 3 && Character.isUpperCase(word.charAt(0))) {
                keywords.add(word);
            }
        }

        return keywords.stream().distinct().limit(20).collect(Collectors.toList());
    }

    /**
     * Fallback resume enhancement without AI
     */
    private Map<String, Object> enhanceResumeFallback(
            Resume resume,
            JobDescription jobDescription,
            List<Project> availableProjects
    ) {
        // Simple skill matching to select projects
        Set<String> jobSkills = new HashSet<>(jobDescription.getKeywords().stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList()));

        List<Project> rankedProjects = availableProjects.stream()
                .sorted((p1, p2) -> {
                    long score1 = p1.getSkills().stream()
                            .filter(s -> jobSkills.contains(s.toLowerCase()))
                            .count();
                    long score2 = p2.getSkills().stream()
                            .filter(s -> jobSkills.contains(s.toLowerCase()))
                            .count();
                    return Long.compare(score2, score1);
                })
                .limit(3)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("selectedProjectIds", rankedProjects.stream()
                .map(Project::getId)
                .collect(Collectors.toList()));
        result.put("keywordsToAdd", jobDescription.getKeywords().stream()
                .limit(5)
                .collect(Collectors.toList()));
        result.put("recommendations", "Projects selected based on skill matching");

        return result;
    }
}
