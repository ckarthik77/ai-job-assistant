package com.jobassist.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobassist.dto.JobDescriptionRequest;
import com.jobassist.model.*;
import com.jobassist.repository.*;
import com.jobassist.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for ResumeController
 */
@WebMvcTest(ResumeController.class)
class ResumeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DocumentParserService documentParserService;

    @MockBean
    private AIEnhancementService aiEnhancementService;

    @MockBean
    private ATSScoreService atsScoreService;

    @MockBean
    private ResumeGeneratorService resumeGeneratorService;

    @MockBean
    private ResumeRepository resumeRepository;

    @MockBean
    private JobDescriptionRepository jobDescriptionRepository;

    @MockBean
    private ProjectRepository projectRepository;

    @MockBean
    private EnhancementSessionRepository sessionRepository;

    private Resume mockResume;
    private JobDescription mockJobDescription;
    private Project mockProject;

    @BeforeEach
    void setUp() {
        // Setup mock resume
        mockResume = new Resume();
        mockResume.setId(1L);
        mockResume.setUserEmail("test@example.com");
        mockResume.setOriginalContent("John Doe\nSoftware Engineer with 5 years of experience");
        mockResume.setSkills(Arrays.asList("Java", "Spring Boot", "React", "PostgreSQL"));
        mockResume.setExperience(List.of(
                Map.of("company", "Tech Corp", "title", "Software Engineer", "dates", "2020-2023")
        ));
        mockResume.setEducation(List.of(
                Map.of("degree", "B.S. Computer Science", "institution", "University", "year", "2019")
        ));

        // Setup mock job description
        mockJobDescription = new JobDescription();
        mockJobDescription.setId(1L);
        mockJobDescription.setCompanyName("Google");
        mockJobDescription.setJobTitle("Senior Software Engineer");
        mockJobDescription.setDescriptionText("We are looking for a Senior Software Engineer...");
        mockJobDescription.setKeywords(Arrays.asList("Java", "Microservices", "Kubernetes", "Docker"));
        mockJobDescription.setRequiredSkills(Arrays.asList("Java", "Spring Boot", "Docker"));

        // Setup mock project
        mockProject = new Project();
        mockProject.setId(1L);
        mockProject.setTitle("E-Commerce Platform");
        mockProject.setDescription("Built a scalable platform handling 50K users...");
        mockProject.setSkills(Arrays.asList("Java", "Spring Boot", "Microservices"));
        mockProject.setCategory("backend");
    }

    @Test
    void testUploadResume_Success() throws Exception {
        // Mock file upload
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                "dummy pdf content".getBytes()
        );

        // Mock service behavior
        when(documentParserService.parseResume(any())).thenReturn(mockResume);
        when(resumeRepository.save(any(Resume.class))).thenReturn(mockResume);

        // Perform request
        mockMvc.perform(multipart("/api/resume/upload")
                        .file(file)
                        .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumeId").value(1))
                .andExpect(jsonPath("$.skillsCount").value(4))
                .andExpect(jsonPath("$.message").exists());

        verify(documentParserService, times(1)).parseResume(any());
        verify(resumeRepository, times(1)).save(any(Resume.class));
    }

    @Test
    void testUploadResume_InvalidFileType() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.txt",
                "text/plain",
                "dummy content".getBytes()
        );

        mockMvc.perform(multipart("/api/resume/upload")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Only PDF and DOCX files are supported"));

        verify(documentParserService, never()).parseResume(any());
    }

    @Test
    void testUploadResume_EmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                new byte[0]
        );

        mockMvc.perform(multipart("/api/resume/upload")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("File is empty"));
    }

    @Test
    void testUploadJobDescription_Success() throws Exception {
        JobDescriptionRequest request = new JobDescriptionRequest();
        request.setCompanyName("Google");
        request.setJobTitle("Senior Software Engineer");
        request.setDescriptionText("We are looking for an experienced engineer...");

        List<String> extractedKeywords = Arrays.asList("Java", "Microservices", "Kubernetes");

        when(aiEnhancementService.extractJobKeywords(anyString())).thenReturn(extractedKeywords);
        when(jobDescriptionRepository.save(any(JobDescription.class))).thenReturn(mockJobDescription);

        mockMvc.perform(post("/api/job/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobDescriptionId").value(1))
                .andExpect(jsonPath("$.keywordCount").value(4))
                .andExpect(jsonPath("$.message").exists());

        verify(aiEnhancementService, times(1)).extractJobKeywords(anyString());
        verify(jobDescriptionRepository, times(1)).save(any(JobDescription.class));
    }

    @Test
    void testUploadJobDescription_MissingDescriptionText() throws Exception {
        JobDescriptionRequest request = new JobDescriptionRequest();
        request.setCompanyName("Google");
        request.setJobTitle("Senior Software Engineer");
        // descriptionText is missing

        mockMvc.perform(post("/api/job/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(aiEnhancementService, never()).extractJobKeywords(anyString());
    }

    @Test
    void testEnhanceResume_Success() throws Exception {
        // Mock repositories
        when(resumeRepository.findById(1L)).thenReturn(Optional.of(mockResume));
        when(jobDescriptionRepository.findById(1L)).thenReturn(Optional.of(mockJobDescription));
        when(projectRepository.findAll()).thenReturn(List.of(mockProject));
        when(projectRepository.findAllById(anyList())).thenReturn(List.of(mockProject));

        // Mock services
        when(atsScoreService.calculateOriginalScore(any(), any())).thenReturn(65);
        when(atsScoreService.calculateEnhancedScore(anyString(), anyList(), any())).thenReturn(85);
        when(atsScoreService.getScoreBreakdown(anyString(), anyList(), anyList(), anyList()))
                .thenReturn(Map.of("totalScore", 85));

        Map<String, Object> enhancement = new HashMap<>();
        enhancement.put("selectedProjectIds", List.of(1L));
        enhancement.put("keywordsToAdd", List.of("Docker", "Kubernetes"));
        enhancement.put("recommendations", "Added relevant projects");
        when(aiEnhancementService.enhanceResumeContent(any(), any(), anyList())).thenReturn(enhancement);

        when(resumeGeneratorService.generateEnhancedResume(any(), anyList(), anyList()))
                .thenReturn("dummy pdf".getBytes());

        EnhancementSession mockSession = new EnhancementSession();
        mockSession.setId(1L);
        mockSession.setOriginalAtsScore(65);
        mockSession.setEnhancedAtsScore(85);
        mockSession.setProcessingTimeMs(5000);
        when(sessionRepository.save(any(EnhancementSession.class))).thenReturn(mockSession);

        // Perform request
        mockMvc.perform(post("/api/enhance")
                        .param("resumeId", "1")
                        .param("jobDescriptionId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(1))
                .andExpect(jsonPath("$.originalAtsScore").value(65))
                .andExpect(jsonPath("$.enhancedAtsScore").value(85))
                .andExpect(jsonPath("$.scoreImprovement").value(20))
                .andExpect(jsonPath("$.addedKeywords").isArray())
                .andExpect(jsonPath("$.addedProjects").isArray());

        verify(resumeRepository, times(1)).findById(1L);
        verify(jobDescriptionRepository, times(1)).findById(1L);
        verify(aiEnhancementService, times(1)).enhanceResumeContent(any(), any(), anyList());
        verify(resumeGeneratorService, times(1)).generateEnhancedResume(any(), anyList(), anyList());
        verify(sessionRepository, times(1)).save(any(EnhancementSession.class));
    }

    @Test
    void testEnhanceResume_ResumeNotFound() throws Exception {
        when(resumeRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/enhance")
                        .param("resumeId", "1")
                        .param("jobDescriptionId", "1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());

        verify(aiEnhancementService, never()).enhanceResumeContent(any(), any(), anyList());
    }

    @Test
    void testGetProjects_Success() throws Exception {
        when(projectRepository.findAll()).thenReturn(List.of(mockProject));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("E-Commerce Platform"))
                .andExpect(jsonPath("$[0].category").value("backend"));

        verify(projectRepository, times(1)).findAll();
    }

    @Test
    void testGetProjects_FilteredByCategory() throws Exception {
        when(projectRepository.findByCategory("backend")).thenReturn(List.of(mockProject));

        mockMvc.perform(get("/api/projects")
                        .param("category", "backend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].category").value("backend"));

        verify(projectRepository, times(1)).findByCategory("backend");
    }

    @Test
    void testHealthCheck() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("AI Job Application Assistant"));
    }

    @Test
    void testDownloadEnhancedResume_Success() throws Exception {
        EnhancementSession mockSession = new EnhancementSession();
        mockSession.setId(1L);
        mockSession.setResume(mockResume);
        mockSession.setAddedKeywords(List.of("Docker", "Kubernetes"));
        mockSession.setAddedProjects(List.of(1L));

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(mockSession));
        when(projectRepository.findAllById(anyList())).thenReturn(List.of(mockProject));
        when(resumeGeneratorService.generateEnhancedResume(any(), anyList(), anyList()))
                .thenReturn("dummy pdf content".getBytes());

        mockMvc.perform(get("/api/enhanced/1/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(content().bytes("dummy pdf content".getBytes()));

        verify(sessionRepository, times(1)).findById(1L);
        verify(resumeGeneratorService, times(1)).generateEnhancedResume(any(), anyList(), anyList());
    }

    @Test
    void testGetEnhancementSession_Success() throws Exception {
        EnhancementSession mockSession = new EnhancementSession();
        mockSession.setId(1L);
        mockSession.setOriginalAtsScore(65);
        mockSession.setEnhancedAtsScore(85);
        mockSession.setAddedKeywords(List.of("Docker", "Kubernetes"));
        mockSession.setAddedProjects(List.of(1L));
        mockSession.setProcessingTimeMs(5000);
        mockSession.setEnhancedResumeUrl("/api/enhanced/1/download");

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(mockSession));
        when(projectRepository.findAllById(anyList())).thenReturn(List.of(mockProject));

        mockMvc.perform(get("/api/enhanced/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(1))
                .andExpect(jsonPath("$.originalAtsScore").value(65))
                .andExpect(jsonPath("$.enhancedAtsScore").value(85))
                .andExpect(jsonPath("$.scoreImprovement").value(20));

        verify(sessionRepository, times(1)).findById(1L);
    }
}
