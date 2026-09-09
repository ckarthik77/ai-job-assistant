package com.jobassist.controller;

import com.jobassist.dto.JobDescriptionRequest;
import com.jobassist.dto.ResumeEnhancementResponse;
import com.jobassist.model.*;
import com.jobassist.repository.*;
import com.jobassist.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * REST Controller for Resume Enhancement API
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "${cors.allowed-origins}")
public class ResumeController {

    private final DocumentParserService documentParserService;
    private final AIEnhancementService aiEnhancementService;
    private final ATSScoreService atsScoreService;
    private final ResumeGeneratorService resumeGeneratorService;

    private final ResumeRepository resumeRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final ProjectRepository projectRepository;
    private final EnhancementSessionRepository sessionRepository;

    /**
     * Upload and parse resume
     */
    @PostMapping(value = "/resume/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "email", required = false) String email
    ) {
        try {
            log.info("Uploading resume: {}", file.getOriginalFilename());

            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
            }

            String filename = file.getOriginalFilename();
            if (filename == null || (!filename.endsWith(".pdf") && !filename.endsWith(".docx"))) {
                return ResponseEntity.badRequest().body(Map.of("error", "Only PDF and DOCX files are supported"));
            }

            // Parse resume
            Resume resume = documentParserService.parseResume(file);
            resume.setUserEmail(email);

            // Save to database
            resume = resumeRepository.save(resume);

            log.info("Resume uploaded successfully: ID {}", resume.getId());

            return ResponseEntity.ok(Map.of(
                    "resumeId", resume.getId(),
                    "skills", resume.getSkills(),
                    "skillsCount", resume.getSkills().size(),
                    "experienceCount", resume.getExperience().size(),
                    "message", "Resume uploaded and parsed successfully"
            ));
        } catch (Exception e) {
            log.error("Error uploading resume", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process resume: " + e.getMessage()));
        }
    }

    /**
     * Upload job description
     */
    @PostMapping("/job/upload")
    public ResponseEntity<Map<String, Object>> uploadJobDescription(
            @Valid @RequestBody JobDescriptionRequest request
    ) {
        try {
            log.info("Processing job description: {}", request.getJobTitle());

            // Extract keywords using AI
            List<String> keywords = aiEnhancementService.extractJobKeywords(request.getDescriptionText());

            // Create job description entity
            JobDescription jobDescription = new JobDescription();
            jobDescription.setCompanyName(request.getCompanyName());
            jobDescription.setJobTitle(request.getJobTitle());
            jobDescription.setDescriptionText(request.getDescriptionText());
            jobDescription.setKeywords(keywords);
            jobDescription.setRequiredSkills(keywords); // For now, same as keywords

            // Save to database
            jobDescription = jobDescriptionRepository.save(jobDescription);

            log.info("Job description saved: ID {}", jobDescription.getId());

            return ResponseEntity.ok(Map.of(
                    "jobDescriptionId", jobDescription.getId(),
                    "keywords", keywords,
                    "keywordCount", keywords.size(),
                    "message", "Job description processed successfully"
            ));
        } catch (Exception e) {
            log.error("Error processing job description", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process job description: " + e.getMessage()));
        }
    }

    /**
     * Enhance resume - main endpoint
     */
    @PostMapping("/enhance")
    public ResponseEntity<?> enhanceResume(
            @RequestParam("resumeId") Long resumeId,
            @RequestParam("jobDescriptionId") Long jobDescriptionId
    ) {
        long startTime = System.currentTimeMillis();

        try {
            log.info("Enhancing resume {} for job {}", resumeId, jobDescriptionId);

            // Fetch resume and job description
            Resume resume = resumeRepository.findById(resumeId)
                    .orElseThrow(() -> new RuntimeException("Resume not found"));
            JobDescription jobDescription = jobDescriptionRepository.findById(jobDescriptionId)
                    .orElseThrow(() -> new RuntimeException("Job description not found"));

            // Calculate original ATS score
            int originalScore = atsScoreService.calculateOriginalScore(resume, jobDescription);

            // Get all available projects
            List<Project> allProjects = projectRepository.findAll();

            // Parallel processing for performance
            CompletableFuture<Map<String, Object>> enhancementFuture = CompletableFuture.supplyAsync(() ->
                    aiEnhancementService.enhanceResumeContent(resume, jobDescription, allProjects)
            );

            Map<String, Object> enhancement = enhancementFuture.get();

            // Get selected projects
            @SuppressWarnings("unchecked")
            List<Long> selectedProjectIds = (List<Long>) enhancement.get("selectedProjectIds");
            List<Project> selectedProjects = projectRepository.findAllById(selectedProjectIds);

            @SuppressWarnings("unchecked")
            List<String> addedKeywords = (List<String>) enhancement.get("keywordsToAdd");

            // Generate enhanced resume PDF
            byte[] pdfBytes = resumeGeneratorService.generateEnhancedResume(
                    resume, selectedProjects, addedKeywords
            );

            // Calculate enhanced ATS score
            List<String> enhancedSkills = resume.getSkills();
            enhancedSkills.addAll(addedKeywords);
            String enhancedContent = resume.getOriginalContent() + "\n" +
                    selectedProjects.stream().map(Project::getDescription).collect(Collectors.joining("\n"));
            int enhancedScore = atsScoreService.calculateEnhancedScore(
                    enhancedContent, enhancedSkills, jobDescription
            );

            // Save enhancement session
            EnhancementSession session = new EnhancementSession();
            session.setResume(resume);
            session.setJobDescription(jobDescription);
            session.setOriginalAtsScore(originalScore);
            session.setEnhancedAtsScore(enhancedScore);
            session.setAddedKeywords(addedKeywords);
            session.setAddedProjects(selectedProjectIds);
            session.setProcessingTimeMs((int) (System.currentTimeMillis() - startTime));
            session.setEnhancedResumeUrl("/api/enhanced/" + resumeId + "/download");

            session = sessionRepository.save(session);

            // Store PDF temporarily (in production, upload to S3 or similar)
            // For now, we'll generate on-demand

            // Get score breakdown
            Map<String, Object> scoreBreakdown = atsScoreService.getScoreBreakdown(
                    enhancedContent, enhancedSkills, jobDescription.getKeywords(), jobDescription.getRequiredSkills()
            );

            // Build response
            ResumeEnhancementResponse response = new ResumeEnhancementResponse();
            response.setSessionId(session.getId());
            response.setOriginalAtsScore(originalScore);
            response.setEnhancedAtsScore(enhancedScore);
            response.setScoreImprovement(enhancedScore - originalScore);
            response.setAddedKeywords(addedKeywords);
            response.setAddedProjects(selectedProjects.stream()
                    .map(p -> new ResumeEnhancementResponse.ProjectInfo(p.getId(), p.getTitle(), p.getCategory()))
                    .collect(Collectors.toList()));
            response.setEnhancedResumeUrl("/api/enhanced/" + session.getId() + "/download");
            response.setProcessingTimeMs(session.getProcessingTimeMs());
            response.setScoreBreakdown(scoreBreakdown);

            log.info("Resume enhancement completed in {}ms. Score: {} -> {}",
                    session.getProcessingTimeMs(), originalScore, enhancedScore);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error enhancing resume", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to enhance resume: " + e.getMessage()));
        }
    }

    /**
     * Download enhanced resume
     */
    @GetMapping("/enhanced/{sessionId}/download")
    public ResponseEntity<byte[]> downloadEnhancedResume(@PathVariable Long sessionId) {
        try {
            EnhancementSession session = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new RuntimeException("Enhancement session not found"));

            Resume resume = session.getResume();
            List<Project> projects = projectRepository.findAllById(session.getAddedProjects());

            // Generate PDF
            byte[] pdfBytes = resumeGeneratorService.generateEnhancedResume(
                    resume, projects, session.getAddedKeywords()
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "enhanced_resume.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            log.error("Error downloading enhanced resume", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get enhancement session details
     */
    @GetMapping("/enhanced/{sessionId}")
    public ResponseEntity<?> getEnhancementSession(@PathVariable Long sessionId) {
        try {
            EnhancementSession session = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new RuntimeException("Enhancement session not found"));

            List<Project> projects = projectRepository.findAllById(session.getAddedProjects());

            ResumeEnhancementResponse response = new ResumeEnhancementResponse();
            response.setSessionId(session.getId());
            response.setOriginalAtsScore(session.getOriginalAtsScore());
            response.setEnhancedAtsScore(session.getEnhancedAtsScore());
            response.setScoreImprovement(session.getEnhancedAtsScore() - session.getOriginalAtsScore());
            response.setAddedKeywords(session.getAddedKeywords());
            response.setAddedProjects(projects.stream()
                    .map(p -> new ResumeEnhancementResponse.ProjectInfo(p.getId(), p.getTitle(), p.getCategory()))
                    .collect(Collectors.toList()));
            response.setEnhancedResumeUrl(session.getEnhancedResumeUrl());
            response.setProcessingTimeMs(session.getProcessingTimeMs());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching enhancement session", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Enhancement session not found"));
        }
    }

    /**
     * Get all available projects (for transparency)
     */
    @GetMapping("/projects")
    public ResponseEntity<List<Map<String, Object>>> getProjects(
            @RequestParam(value = "category", required = false) String category
    ) {
        try {
            List<Project> projects = category != null ?
                    projectRepository.findByCategory(category) :
                    projectRepository.findAll();

            List<Map<String, Object>> projectList = projects.stream()
                    .map(p -> Map.of(
                            "id", p.getId(),
                            "title", p.getTitle(),
                            "description", p.getDescription(),
                            "skills", p.getSkills(),
                            "category", p.getCategory()
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(projectList);

        } catch (Exception e) {
            log.error("Error fetching projects", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "AI Job Application Assistant"
        ));
    }
}
