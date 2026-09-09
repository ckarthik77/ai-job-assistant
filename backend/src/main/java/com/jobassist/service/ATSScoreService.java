package com.jobassist.service;

import com.jobassist.model.JobDescription;
import com.jobassist.model.Resume;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service for calculating ATS (Applicant Tracking System) compatibility scores
 *
 * Scoring breakdown:
 * - Keyword Match: 40% (how many job keywords appear in resume)
 * - Skill Coverage: 30% (resume skills vs required skills)
 * - Format Compliance: 20% (proper sections, structure, readability)
 * - XYZ Format Usage: 10% (percentage of achievements using Google XYZ format)
 */
@Service
@Slf4j
public class ATSScoreService {

    private static final int KEYWORD_WEIGHT = 40;
    private static final int SKILL_WEIGHT = 30;
    private static final int FORMAT_WEIGHT = 20;
    private static final int XYZ_WEIGHT = 10;

    /**
     * Calculate ATS score for original resume
     */
    public int calculateOriginalScore(Resume resume, JobDescription jobDescription) {
        return calculateScore(
                resume.getOriginalContent(),
                resume.getSkills(),
                jobDescription.getKeywords(),
                jobDescription.getRequiredSkills()
        );
    }

    /**
     * Calculate ATS score for enhanced resume
     */
    public int calculateEnhancedScore(
            String enhancedResumeContent,
            List<String> enhancedSkills,
            JobDescription jobDescription
    ) {
        return calculateScore(
                enhancedResumeContent,
                enhancedSkills,
                jobDescription.getKeywords(),
                jobDescription.getRequiredSkills()
        );
    }

    /**
     * Main scoring algorithm
     */
    private int calculateScore(
            String resumeContent,
            List<String> resumeSkills,
            List<String> jobKeywords,
            List<String> requiredSkills
    ) {
        int keywordScore = calculateKeywordScore(resumeContent, jobKeywords);
        int skillScore = calculateSkillScore(resumeSkills, requiredSkills);
        int formatScore = calculateFormatScore(resumeContent);
        int xyzScore = calculateXYZScore(resumeContent);

        int totalScore = keywordScore + skillScore + formatScore + xyzScore;

        log.info("ATS Score breakdown - Keyword: {}, Skill: {}, Format: {}, XYZ: {}, Total: {}",
                keywordScore, skillScore, formatScore, xyzScore, totalScore);

        return Math.min(100, Math.max(0, totalScore));
    }

    /**
     * Calculate keyword match score (40 points max)
     */
    private int calculateKeywordScore(String resumeContent, List<String> jobKeywords) {
        if (jobKeywords == null || jobKeywords.isEmpty()) {
            return KEYWORD_WEIGHT;
        }

        String resumeLower = resumeContent.toLowerCase();
        long matchedCount = jobKeywords.stream()
                .filter(keyword -> resumeLower.contains(keyword.toLowerCase()))
                .count();

        double matchPercentage = (double) matchedCount / jobKeywords.size();
        return (int) (matchPercentage * KEYWORD_WEIGHT);
    }

    /**
     * Calculate skill coverage score (30 points max)
     */
    private int calculateSkillScore(List<String> resumeSkills, List<String> requiredSkills) {
        if (requiredSkills == null || requiredSkills.isEmpty()) {
            return SKILL_WEIGHT;
        }

        Set<String> resumeSkillSet = resumeSkills.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        long matchedCount = requiredSkills.stream()
                .filter(skill -> resumeSkillSet.contains(skill.toLowerCase()))
                .count();

        double matchPercentage = (double) matchedCount / requiredSkills.size();
        return (int) (matchPercentage * SKILL_WEIGHT);
    }

    /**
     * Calculate format compliance score (20 points max)
     */
    private int calculateFormatScore(String resumeContent) {
        int score = 0;

        // Check for essential sections (5 points each)
        if (containsSection(resumeContent, "experience|work history|employment")) {
            score += 5;
        }
        if (containsSection(resumeContent, "education|academic")) {
            score += 5;
        }
        if (containsSection(resumeContent, "skills|technical skills|competencies")) {
            score += 5;
        }

        // Check for bullet points (3 points)
        if (resumeContent.contains("•") || resumeContent.contains("-") || resumeContent.contains("*")) {
            score += 3;
        }

        // Check for proper formatting (dates, structure) (2 points)
        if (containsDatePattern(resumeContent)) {
            score += 2;
        }

        return Math.min(FORMAT_WEIGHT, score);
    }

    /**
     * Calculate XYZ format usage score (10 points max)
     */
    private int calculateXYZScore(String resumeContent) {
        // Look for XYZ format patterns: "Accomplished X as measured by Y, by doing Z"
        // Also look for quantified achievements with metrics

        List<String> bullets = extractBulletPoints(resumeContent);
        if (bullets.isEmpty()) {
            return 0;
        }

        long xyzFormattedCount = bullets.stream()
                .filter(this::isXYZFormat)
                .count();

        double xyzPercentage = (double) xyzFormattedCount / bullets.size();
        return (int) (xyzPercentage * XYZ_WEIGHT);
    }

    /**
     * Check if text contains a specific section
     */
    private boolean containsSection(String text, String sectionPattern) {
        Pattern pattern = Pattern.compile("(?i)\\b(" + sectionPattern + ")\\b", Pattern.CASE_INSENSITIVE);
        return pattern.matcher(text).find();
    }

    /**
     * Check if text contains date patterns (e.g., 2020-2023, Jan 2020 - Present)
     */
    private boolean containsDatePattern(String text) {
        Pattern datePattern = Pattern.compile(
                "\\b(\\d{4}\\s*[-–]\\s*(?:\\d{4}|Present|present|Current|current))|" +
                "((?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\\s+\\d{4})"
        );
        return datePattern.matcher(text).find();
    }

    /**
     * Extract bullet points from resume content
     */
    private List<String> extractBulletPoints(String text) {
        List<String> bullets = new ArrayList<>();
        String[] lines = text.split("\n");

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("•") || trimmed.startsWith("-") || trimmed.startsWith("*")) {
                bullets.add(trimmed.substring(1).trim());
            }
        }

        return bullets;
    }

    /**
     * Check if a bullet point follows XYZ format
     * Looks for: action verb + quantified result + method
     */
    private boolean isXYZFormat(String bullet) {
        // Check for quantified achievements (numbers, percentages, metrics)
        boolean hasMetrics = containsMetrics(bullet);

        // Check for action verbs at the beginning
        boolean hasActionVerb = startsWithActionVerb(bullet);

        // Check for "by" clause indicating methodology
        boolean hasByClause = bullet.toLowerCase().contains(" by ");

        // XYZ format typically has at least metrics and either action verb or by clause
        return hasMetrics && (hasActionVerb || hasByClause);
    }

    /**
     * Check if text contains metrics/numbers
     */
    private boolean containsMetrics(String text) {
        Pattern metricsPattern = Pattern.compile(
                "\\d+%|" +                          // Percentages
                "\\$\\d+|" +                        // Dollar amounts
                "\\d+\\+?\\s*(million|thousand|k|M)|" +  // Large numbers
                "\\d+x|" +                          // Multipliers
                "\\d+\\s*(users?|customers?|requests?|transactions?|items?|records?|hours?|days?|weeks?|months?)"  // Counts
        );
        return metricsPattern.matcher(text).find();
    }

    /**
     * Check if text starts with an action verb
     */
    private boolean startsWithActionVerb(String text) {
        String[] actionVerbs = {
                "achieved", "accomplished", "architected", "built", "created", "designed",
                "developed", "engineered", "established", "implemented", "improved",
                "increased", "launched", "led", "managed", "optimized", "reduced",
                "streamlined", "transformed", "delivered", "drove", "executed",
                "spearheaded", "pioneered", "automated", "scaled", "migrated"
        };

        String lowerText = text.toLowerCase();
        for (String verb : actionVerbs) {
            if (lowerText.startsWith(verb)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get detailed score breakdown
     */
    public Map<String, Object> getScoreBreakdown(
            String resumeContent,
            List<String> resumeSkills,
            List<String> jobKeywords,
            List<String> requiredSkills
    ) {
        Map<String, Object> breakdown = new HashMap<>();

        int keywordScore = calculateKeywordScore(resumeContent, jobKeywords);
        int skillScore = calculateSkillScore(resumeSkills, requiredSkills);
        int formatScore = calculateFormatScore(resumeContent);
        int xyzScore = calculateXYZScore(resumeContent);
        int totalScore = keywordScore + skillScore + formatScore + xyzScore;

        breakdown.put("keywordScore", keywordScore);
        breakdown.put("keywordMax", KEYWORD_WEIGHT);
        breakdown.put("skillScore", skillScore);
        breakdown.put("skillMax", SKILL_WEIGHT);
        breakdown.put("formatScore", formatScore);
        breakdown.put("formatMax", FORMAT_WEIGHT);
        breakdown.put("xyzScore", xyzScore);
        breakdown.put("xyzMax", XYZ_WEIGHT);
        breakdown.put("totalScore", totalScore);
        breakdown.put("totalMax", 100);

        return breakdown;
    }
}
